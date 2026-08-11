package com.pavlo.regionsmapdownloader

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.commit
import com.pavlo.regionsmapdownloader.ui.RegionsListFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        val statusBarScrim = findViewById<View>(R.id.statusBarScrim)
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragmentContainer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootContainer)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            statusBarScrim.layoutParams = statusBarScrim.layoutParams.apply {
                height = systemBars.top
            }
            fragmentContainer.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, RegionsListFragment.newInstance())
            }
        }
    }

    fun configureToolbar(title: String, showBackButton: Boolean) {
        supportActionBar?.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(showBackButton)
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener(
            if (showBackButton) View.OnClickListener { onBackPressedDispatcher.onBackPressed() } else null
        )
    }
}