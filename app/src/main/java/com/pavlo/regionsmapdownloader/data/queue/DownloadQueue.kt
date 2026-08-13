package com.pavlo.regionsmapdownloader.data.queue

import com.pavlo.regionsmapdownloader.domain.data_source.DownloadQueueStorage
import com.pavlo.regionsmapdownloader.domain.data_source.MapStorage
import com.pavlo.regionsmapdownloader.domain.model.DownloadOutcome
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueEvent
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueItem
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class DownloadQueue(
    private val storage: DownloadQueueStorage,
    private val mapStorage: MapStorage,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val restored = AtomicBoolean(false)

    private val _state = MutableStateFlow(DownloadQueueState())
    val state: StateFlow<DownloadQueueState> = _state.asStateFlow()

    private val _completed = MutableStateFlow<Set<String>>(emptySet())
    val completed: StateFlow<Set<String>> = _completed.asStateFlow()

    private val _events = MutableSharedFlow<DownloadQueueEvent>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<DownloadQueueEvent> = _events.asSharedFlow()

    suspend fun restore() {
        if (!restored.compareAndSet(false, true)) return
        val (saved, downloaded) = withContext(ioDispatcher) {
            storage.load() to mapStorage.downloadedNames()
        }
        _state.update { current ->
            val restoredItems = saved.filterNot { current.contains(it.downloadName) }
            current.copy(pending = restoredItems + current.pending)
        }
        _completed.value = downloaded
    }

    suspend fun enqueue(item: DownloadQueueItem): Boolean {
        var added = false
        val updated = _state.updateAndGet { current ->
            added = !current.contains(item.downloadName)
            if (added) current.copy(pending = current.pending + item) else current
        }
        if (!added) return false

        _completed.update { it - item.downloadName }
        persist(updated)
        return true
    }

    suspend fun takeNext(): DownloadQueueItem? {
        val updated = _state.updateAndGet { current ->
            current.copy(
                active = current.pending.firstOrNull(),
                activePercent = 0,
                pending = current.pending.drop(1)
            )
        }
        persist(updated)
        return updated.active
    }

    fun updateProgress(percent: Int) {
        _state.update { current ->
            if (current.active == null) current else current.copy(activePercent = percent)
        }
    }

    suspend fun finishActive(outcome: DownloadOutcome) {
        var finished: DownloadQueueItem? = null
        val updated = _state.updateAndGet { current ->
            finished = current.active
            current.copy(active = null, activePercent = 0)
        }
        val item = finished ?: return

        if (outcome is DownloadOutcome.Success) {
            _completed.update { it + item.downloadName }
        }
        persist(updated)
        if (outcome is DownloadOutcome.Failure) {
            _events.emit(DownloadQueueEvent.Failed(item, outcome.message))
        }
    }

    suspend fun cancel(downloadName: String) {
        val updated = _state.updateAndGet { current ->
            if (current.active?.downloadName == downloadName) {
                current.copy(active = null, activePercent = 0)
            } else {
                current.copy(pending = current.pending.filterNot { it.downloadName == downloadName })
            }
        }
        persist(updated)
    }

    private suspend fun persist(state: DownloadQueueState) = withContext(ioDispatcher) {
        storage.save(state.items)
    }

    private companion object {
        const val EVENT_BUFFER = 16
    }
}
