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

    private val viewModel: RegionsViewModel by activityViewModels {
        val app = requireActivity().application as RegionApplication
        RegionsViewModelFactory(
            app.appInitializer.getAllRegionsUseCase,
            app.appInitializer.getDeviceMemoryInfoUseCase
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val path = regionPath
        val isRoot = path.isEmpty()

        val deviceMemoryContainer = view.findViewById<View>(R.id.deviceMemoryContainer)
        val sectionDivider = view.findViewById<View>(R.id.sectionDivider)
        deviceMemoryContainer.visibility = if (isRoot) View.VISIBLE else View.GONE
        sectionDivider.visibility = if (isRoot) View.VISIBLE else View.GONE

        // Initializing regions recycler view
        val regionsRecyclerView = view.findViewById<RecyclerView>(R.id.regionRecyclerView)
        regionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = RegionListAdapter(
            items = emptyList(),
            onDownloadClick = { },
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

        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val errorTextView = view.findViewById<TextView>(R.id.errorTextView)

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
                                val region = state.regions.findByPath(path)
                                (requireActivity() as MainActivity).configureToolbar(
                                    title = region?.name.orEmpty(),
                                    showBackButton = true
                                )
                                adapter.updateItems(
                                    region?.subRegions.orEmpty()
                                        .map { RegionListItem.RegionRow(it, path + it.name) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isRoot) {
            val deviceMemoryProgressBar = view.findViewById<ProgressBar>(R.id.deviceMemoryProgressBar)
            val freeSpaceTextView = view.findViewById<TextView>(R.id.freeSpaceTextView)

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.memoryInfo.collect { memoryInfo ->
                        memoryInfo ?: return@collect
                        deviceMemoryProgressBar.progress = memoryInfo.usedPercent
                        freeSpaceTextView.text = getString(
                            R.string.device_free_space_format,
                            StringFormatHelper.formatGb(memoryInfo.freeBytes)
                        )
                    }
                }
            }

            viewModel.loadMemoryInfo()
        }

        if (viewModel.state.value !is RegionsListUiState.Success) {
            viewModel.loadRegions()
        }
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