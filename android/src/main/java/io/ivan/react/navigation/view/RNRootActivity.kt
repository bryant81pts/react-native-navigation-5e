package io.ivan.react.navigation.view

import android.os.Bundle
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap
import io.ivan.react.navigation.R
import io.ivan.react.navigation.bridge.NavigationConstants
import io.ivan.react.navigation.model.Root
import io.ivan.react.navigation.model.RootType
import io.ivan.react.navigation.model.Screen
import io.ivan.react.navigation.model.Tabs
import io.ivan.react.navigation.utils.*


class RNRootActivity : RNBaseActivity() {

    private lateinit var navHostFragment: NavHostFragment
    private var startDestination: NavDestination? = null

    private val navController: NavController
        get() = navHostFragment.navController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navHostFragment = createNavHostFragmentWithoutGraph()

        supportFragmentManager.beginTransaction()
            .add(Window.ID_ANDROID_CONTENT, navHostFragment)
            .setPrimaryNavigationFragment(navHostFragment)
            .commit()

        receive()
    }

    private fun receive() {
        Store.reducer(ACTION_REGISTER_REACT_COMPONENT)?.observe(this, { state ->
            val pair = state as Pair<String, ReadableMap>
        })

        Store.reducer(ACTION_SET_ROOT)?.observe(this, { state ->
            with(state as Root) {
                when {
                    this is Tabs && type == RootType.TABS -> {
                        val tabBarModuleName = options?.optString("tabBarModuleName")
                        navController.setStartDestination(buildDestinationWithTabBar())
                    }
                    this is Screen && type == RootType.STACK -> {
                        navController.setStartDestination(buildDestination(page.rootName))
                    }
                    this is Screen && type == RootType.SCREEN -> {
                        navController.setStartDestination(buildDestination(page.rootName))
                    }
                }
            }
        })

        Store.reducer(ACTION_CURRENT_ROUTE)?.observe(this, { state ->
            val promise = state as Promise
            promise.resolve(navController.currentDestination?.id)
        })

        Store.reducer(ACTION_SET_RESULT)?.observe(this, { state ->
            val data = state as ReadableMap

            sendNavigationEvent(
                NavigationConstants.COMPONENT_RESULT,
                navController.previousBackStackEntry?.destination?.id?.toString(),
                Arguments.createMap().also { it.merge(data) },
                NavigationConstants.RESULT_TYPE_OK
            )
        })

        dispatch()
    }

    private fun dispatch() {
        Store.reducer(ACTION_DISPATCH_PUSH)?.observe(this, { state ->
            val data = state as Pair<String, ReadableMap>
            val destinationName = data.first
            val destination = buildDestination(destinationName)
            navController.graph.addDestination(destination)
            navController.navigate(destination.id)
        })

        Store.reducer(ACTION_DISPATCH_POP)?.observe(this, {
            navController.popBackStack()
        })

        Store.reducer(ACTION_DISPATCH_POP_TO_ROOT)?.observe(this, {
            startDestination?.let {
                navController.navigate(it.id)
            }
        })

        Store.reducer(ACTION_DISPATCH_PRESENT)?.observe(this, { state ->
            val data = state as Pair<String, ReadableMap>
            val destinationName = data.first
            val destination = buildDestination(destinationName)
            navController.graph.addDestination(destination)
            val navOptionsBuilder = navOptions {
                anim {
                    enter = R.anim.navigation_top_enter
                    exit = android.R.anim.fade_out
                    popEnter = android.R.anim.fade_in
                    popExit = R.anim.navigation_top_exit
                }
            }
            navController.navigate(destination.id, null, navOptionsBuilder)
        })

        Store.reducer(ACTION_DISPATCH_DISMISS)?.observe(this, {
            navController.popBackStack()
        })
    }

    private fun buildDestination(destinationName: String): NavDestination {
        return buildDestination(this, supportFragmentManager, destinationName)
    }

    private fun buildDestinationWithTabBar(): NavDestination {
        return FragmentNavigator(this, supportFragmentManager, R.id.content).createDestination().also {
            val viewId = ViewCompat.generateViewId()
            it.id = viewId
            it.className = RNTabBarFragment::class.java.name
        }
    }

}
