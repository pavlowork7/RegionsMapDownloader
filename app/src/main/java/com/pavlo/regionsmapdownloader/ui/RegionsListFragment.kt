package com.pavlo.regionsmapdownloader.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pavlo.regionsmapdownloader.R
import com.pavlo.regionsmapdownloader.data.model.RegionItem

class RegionsListFragment: Fragment(R.layout.region_list_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initializing regions recycler view
        val regionsRecyclerView = view.findViewById<RecyclerView>(R.id.regionRecyclerView)
        regionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val items = listOf(
            RegionItem("Albania", true),
            RegionItem("Albania 2", true),
            RegionItem("Albania 3", false)
        )

        // TODO: Implement later
        regionsRecyclerView.adapter = RegionsRecyclerViewAdapter(
            items = items,
            onItemClick = { }
        )
    }

    companion object {
        fun newInstance(): RegionsListFragment {
            return RegionsListFragment()
        }
    }
}