package com.pavlo.regionsmapdownloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pavlo.regionsmapdownloader.databinding.ActivityMainBinding
import com.pavlo.regionsmapdownloader.navigation.FragmentNavigator
import com.pavlo.regionsmapdownloader.navigation.Navigator
import com.pavlo.regionsmapdownloader.navigation.NavigatorProvider
import com.pavlo.regionsmapdownloader.navigation.ToolbarHost
import com.pavlo.regionsmapdownloader.navigation.ToolbarState

class MainActivity : AppCompatActivity(), NavigatorProvider, ToolbarHost {

    private lateinit var binding: ActivityMainBinding

    override val navigator: Navigator by lazy {
        FragmentNavigator(supportFragmentManager, R.id.fragmentContainer)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setSupportActionBar(binding.toolbar)

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootContainer) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.statusBarScrim.layoutParams = binding.statusBarScrim.layoutParams.apply {
                height = systemBars.top
            }
            binding.fragmentContainer.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            navigator.openRegionList()
        }

        requestNotificationPermissionIfNeeded()
    }

    override fun configureToolbar(state: ToolbarState) {
        supportActionBar?.title = state.title
        supportActionBar?.setDisplayHomeAsUpEnabled(state.showBackButton)
        binding.toolbar.setNavigationOnClickListener(
            if (state.showBackButton) {
                View.OnClickListener { onBackPressedDispatcher.onBackPressed() }
            } else {
                null
            }
        )
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
