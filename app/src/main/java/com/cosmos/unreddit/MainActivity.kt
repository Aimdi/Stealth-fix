package com.cosmos.unreddit

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cosmos.unreddit.databinding.ActivityMainBinding
import com.cosmos.unreddit.ui.postlist.PostListFragment
import com.cosmos.unreddit.util.HideBottomViewBehavior
import com.cosmos.unreddit.util.extension.clearWindowInsetsListener
import com.cosmos.unreddit.util.extension.currentNavigationFragment
import com.cosmos.unreddit.util.extension.launchRepeat
import com.cosmos.unreddit.util.extension.unredditApplication
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: UiViewModel by viewModels()

    private lateinit var navController: NavController

    private var bottomNavigationInitialized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(unredditApplication.appTheme)
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()
        initBottomNavigationView()

        launchRepeat(Lifecycle.State.STARTED) {
            launch {
                viewModel.navigationVisibility
                    // Drop the first item to let initBottomNavigationView manage the visibility
                    .drop(1)
                    .collect(this@MainActivity::showNavigation)
            }
        }
    }

    private fun initNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                as NavHostFragment
        navController = navHostFragment.navController.apply {
            addOnDestinationChangedListener(this@MainActivity)
        }

        binding.bottomNavigation.run {
            setupWithNavController(navController)
            setOnItemReselectedListener {
                when (it.itemId) {
                    R.id.home -> (currentNavigationFragment as? PostListFragment)?.scrollToTop()
                    else -> {
                        // Ignore
                    }
                }
            }
        }
    }

    /**
     * Full-width bottom bar (Reddit mobile web style). System bar inset is applied as
     * padding so icons sit above the gesture/nav area without floating side margins.
     */
    private fun initBottomNavigationView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.run {
                setPadding(paddingLeft, paddingTop, paddingRight, insets.bottom)
                updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = 0
                }
                clearWindowInsetsListener()
            }

            windowInsets
        }

        binding.bottomNavigation.updateLayoutParams<CoordinatorLayout.LayoutParams> {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            gravity = Gravity.BOTTOM
            behavior = HideBottomViewBehavior<BottomNavigationView>(false)
        }

        // Wait for the view to be ready to show/hide it
        binding.bottomNavigation.post {
            showNavigation(viewModel.navigationVisibility.value, false)
        }

        bottomNavigationInitialized = true
    }

    private fun showNavigation(show: Boolean, animate: Boolean = true) {
        val layoutParams = binding.bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = layoutParams.behavior as HideBottomViewBehavior?

        if (show) {
            behavior?.run {
                enabled = true
                slideIn(binding.bottomNavigation, animate)
            }
        } else {
            behavior?.run {
                enabled = false
                slideOut(binding.bottomNavigation, animate)
            }
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.postListFragment,
            R.id.subscriptionsFragment,
            R.id.profileFragment,
            R.id.preferencesFragment -> {
                viewModel.setNavigationVisibility(true)
            }

            else -> viewModel.setNavigationVisibility(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomNavigationInitialized = false
    }
}
