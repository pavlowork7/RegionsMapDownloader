package com.pavlo.regionsmapdownloader.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.pavlo.regionsmapdownloader.ui.RegionsListFragment

class FragmentNavigator(
    private val fragmentManager: FragmentManager,
    @IdRes private val containerId: Int
) : Navigator {

    override fun openRegionList(path: List<String>) {
        fragmentManager.commit {
            replace(containerId, RegionsListFragment.newInstance(path))
            if (path.isNotEmpty()) {
                addToBackStack(path.last())
            }
        }
    }
}
