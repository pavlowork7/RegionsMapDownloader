package com.pavlo.regionsmapdownloader.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavlo.regionsmapdownloader.MainActivity
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.RegionApplication
import com.pavlo.regionsmapdownloader.ui.utils.StringFormatHelper
import kotlinx.coroutines.launch

class RegionsListFragment: Fragment(R.layout.region_list_main_fragment) {

    private val regionPath: List<String>
        get() = arguments?.getStringArrayList(ARG_REGION_PATH).orEmpty()

    private val isRoot: Boolean
        get() = regionPath.isEmpty()

    private val viewModel: RegionsViewModel by activityViewModels {
        val app = requireActivity().application as RegionApplication
        RegionsViewModelFactory(
            app.appInitializer.getAllRegionsUseCase,
            app.appInitializer.getDeviceMemoryInfoUseCase,
            app.appInitializer.regionDownloadScheduler,
            app.appInitializer.workManager
        )
    }

    private lateinit var regionsRecyclerView: RecyclerView
    private lateinit var adapter: RegionListAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var freeSpaceTextView: TextView
    private lateinit var deviceMemoryProgressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deviceMemoryContainer = view.findViewById<View>(R.id.deviceMemoryContainer)
        val sectionDivider = view.findViewById<View>(R.id.sectionDivider)
        deviceMemoryContainer.visibility = if (isRoot) View.VISIBLE else View.GONE
        sectionDivider.visibility = if (isRoot) View.VISIBLE else View.GONE

        initializeViews(view)

        regionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = RegionListAdapter(
            items = emptyList(),
            onDownloadClick = { regionKey, url, destinationPath -> viewModel.startDownload(regionKey, url, destinationPath) },
            onCancelClick = { regionKey -> viewModel.cancelDownload(regionKey) },
            onRegionClick = { row ->
                if (row.region.hasChildren) {
                    parentFragmentManager.commit {
                        replace(R.id.fragmentContainer, newInstance(row.path))
                        addToBackStack(row.region.name)
                    }
                }
            }
        )
        regionsRecyclerView.adapter = adapter

        observeRegions()
        observeDownloadProgress()
        observeCompletedRegions()

        if (isRoot) {
            observeDeviceMemoryInfo()
            viewModel.loadMemoryInfo()
        }

        if (viewModel.state.value !is RegionsListUiState.Success) {
            viewModel.loadRegions()
        }
    }

    private fun observeDownloadProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.downloadProgress.collect { progressMap ->
                    adapter.setDownloadProgress(progressMap)
                }
            }
        }
    }

    private fun observeCompletedRegions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.completedRegions.collect { completedRegions ->
                    completedRegions.forEach { regionId ->
                        adapter.markCompleted(regionId)
                    }
                }
            }
        }
    }

    private fun observeRegions() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is RegionsListUiState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            errorTextView.visibility = View.GONE
                        }
                        is RegionsListUiState.Error -> {
                            progressBar.visibility = View.GONE
                            errorTextView.visibility = View.VISIBLE
                            errorTextView.text = state.message
                        }
                        is RegionsListUiState.Success -> {
                            progressBar.visibility = View.GONE
                            errorTextView.visibility = View.GONE

                            if (isRoot) {
                                (requireActivity() as MainActivity).configureToolbar(
                                    title = getString(R.string.download_maps_title),
                                    showBackButton = false
                                )
                                adapter.updateItems(state.regions.toListItems())
                            } else {
                                val region = state.regions.findByPath(regionPath)
                                (requireActivity() as MainActivity).configureToolbar(
                                    title = region?.displayName.orEmpty(),
                                    showBackButton = true
                                )
                                adapter.updateItems(
                                    region?.subRegions.orEmpty()
                                        .map { RegionListItem.RegionRow(it, regionPath + it.name) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeDeviceMemoryInfo() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.memoryInfo.collect { memoryInfo ->
                    memoryInfo ?: return@collect
                    deviceMemoryProgressBar.progress = memoryInfo.usedPercent
                    freeSpaceTextView.text = getString(
                        R.string.device_free_space_format,
                        StringFormatHelper.formatGb(memoryInfo.freeBytes, getString(R.string.gb_format))
                    )
                }
            }
        }
    }

    private fun initializeViews(view: View) {
        regionsRecyclerView = view.findViewById(R.id.regionRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.errorTextView)
        deviceMemoryProgressBar = view.findViewById(R.id.deviceMemoryProgressBar)
        freeSpaceTextView = view.findViewById(R.id.freeSpaceTextView)
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