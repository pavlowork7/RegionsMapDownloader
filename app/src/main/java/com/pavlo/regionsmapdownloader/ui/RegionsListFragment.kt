package com.pavlo.regionsmapdownloader.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.snackbar.Snackbar
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.RegionApplication
import com.pavlo.regionsmapdownloader.databinding.RegionListMainFragmentBinding
import com.pavlo.regionsmapdownloader.domain.model.DownloadQueueEvent
import com.pavlo.regionsmapdownloader.navigation.Navigator
import com.pavlo.regionsmapdownloader.navigation.NavigatorProvider
import com.pavlo.regionsmapdownloader.navigation.ToolbarHost
import com.pavlo.regionsmapdownloader.navigation.ToolbarState
import com.pavlo.regionsmapdownloader.ui.utils.StringFormatHelper
import com.pavlo.regionsmapdownloader.ui.utils.requireHost
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class RegionsListFragment : Fragment() {

    private var _binding: RegionListMainFragmentBinding? = null
    private val binding get() = _binding!!

    private val regionPath: List<String>
        get() = arguments?.getStringArrayList(ARG_REGION_PATH).orEmpty()

    private val isRoot: Boolean
        get() = regionPath.isEmpty()

    private val viewModel: RegionsViewModel by activityViewModels {
        viewModelFactory {
            initializer {
                val di = (requireActivity().application as RegionApplication).appInitializer
                RegionsViewModel(
                    di.getAllRegionsUseCase,
                    di.getDeviceMemoryInfoUseCase,
                    di.regionDownloadScheduler,
                    di.downloadQueue
                )
            }
        }
    }

    private lateinit var navigator: Navigator
    private lateinit var toolbarHost: ToolbarHost

    private lateinit var adapter: RegionListAdapter

    private var appliedToolbarState: ToolbarState? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigator = requireHost<NavigatorProvider>().navigator
        toolbarHost = requireHost<ToolbarHost>()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = RegionListMainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appliedToolbarState = null
        binding.deviceMemoryContainer.visibility = if (isRoot) View.VISIBLE else View.GONE
        binding.sectionDivider.visibility = if (isRoot) View.VISIBLE else View.GONE

        binding.regionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RegionListAdapter(
            onDownloadClick = { downloadName, displayName -> viewModel.startDownload(downloadName, displayName) },
            onCancelClick = { downloadName -> viewModel.cancelDownload(downloadName) },
            onRegionClick = { row ->
                if (row.region.hasChildren) {
                    navigator.openRegionList(row.path)
                }
            }
        )
        binding.regionRecyclerView.adapter = adapter
        (binding.regionRecyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false

        observeListItems()
        observeEvents()

        if (isRoot) {
            observeDeviceMemoryInfo()
            viewModel.loadMemoryInfo()
            setToolbar(getString(R.string.download_maps_title), showBackButton = false)
        }

        if (viewModel.state.value !is RegionsListUiState.Success) {
            viewModel.loadRegions()
        }
    }

    private fun observeListItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.state,
                    viewModel.downloadProgress,
                    viewModel.completedRegions
                ) { state, progress, completed -> Triple(state, progress, completed) }
                    .collect { (state, progress, completed) -> renderState(state, progress, completed) }
            }
        }
    }

    private fun renderState(state: RegionsListUiState, progress: Map<String, Int>, completed: Set<String>) {
        when (state) {
            is RegionsListUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorTextView.visibility = View.GONE
            }
            is RegionsListUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.VISIBLE
                binding.errorTextView.text = state.message
            }
            is RegionsListUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.errorTextView.visibility = View.GONE

                if (isRoot) {
                    adapter.submitList(state.regions.toListItems(progress, completed))
                } else {
                    val region = state.regions.findByPath(regionPath)
                    setToolbar(region?.displayName.orEmpty(), showBackButton = true)
                    adapter.submitList(region?.subRegions.orEmpty().toRegionRows(regionPath, progress, completed))
                }
            }
        }
    }

    private fun setToolbar(title: String, showBackButton: Boolean) {
        val state = ToolbarState(title, showBackButton)
        if (state == appliedToolbarState) return
        appliedToolbarState = state
        toolbarHost.configureToolbar(state)
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is DownloadQueueEvent.Failed -> Snackbar
                            .make(
                                binding.root,
                                getString(R.string.download_failed_format, event.item.displayName),
                                Snackbar.LENGTH_LONG
                            )
                            .setAction(R.string.retry) {
                                viewModel.startDownload(event.item.downloadName, event.item.displayName)
                            }
                            .show()
                    }
                }
            }
        }
    }

    private fun observeDeviceMemoryInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.memoryInfo.collect { memoryInfo ->
                    memoryInfo ?: return@collect
                    binding.deviceMemoryProgressBar.progress = memoryInfo.usedPercent
                    binding.freeSpaceTextView.text = getString(
                        R.string.device_free_space_format,
                        StringFormatHelper.formatGb(memoryInfo.freeBytes, getString(R.string.gb_format))
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_REGION_PATH = "region_path"

        fun newInstance(path: List<String> = emptyList()): RegionsListFragment {
            return RegionsListFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_REGION_PATH, ArrayList(path))
                }
            }
        }
    }
}
