package com.pavlo.regionsmapdownloader.ui

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.RegionApplication
import kotlinx.coroutines.launch

class RegionsListFragment: Fragment(R.layout.region_list_fragment) {

    private val viewModel: RegionsViewModel by viewModels {
        val app = requireActivity().application as RegionApplication
        RegionsViewModelFactory(app.appInitializer.getAllRegionsUseCase)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializing regions recycler view
        val regionsRecyclerView = view.findViewById<RecyclerView>(R.id.regionRecyclerView)
        regionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = RegionListAdapter(
            items = emptyList(),
            onItemClick = { }
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
                            adapter.updateItems(state.regions.toListItems())
                        }
                    }
                }
            }
        }


        viewModel.loadContent()
    }

    companion object {
        fun newInstance(): RegionsListFragment {
            return RegionsListFragment()
        }
    }
}