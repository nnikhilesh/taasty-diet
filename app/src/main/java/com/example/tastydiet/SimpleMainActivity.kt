package com.example.tastydiet

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.ui.HomeScreen
import com.example.tastydiet.ui.theme.TastyDietTheme
import com.example.tastydiet.viewmodel.SimpleFoodLogViewModel

class SimpleMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TastyDietTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SimpleApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleApp() {
    val database = AppDatabase.getInstance(LocalContext.current.applicationContext as Application)
    val viewModel: SimpleFoodLogViewModel = viewModel {
        SimpleFoodLogViewModel(
            foodLogDao = database.foodLogDao(),
            nutritionalInfoDao = database.nutritionalInfoDao(),
            profileDao = database.profileDao()
        )
    }
    
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val items = listOf(
                    NavigationItem(
                        title = "Home",
                        icon = Icons.Default.Home,
                        route = "home"
                    ),
                    NavigationItem(
                        title = "Recipes",
                        icon = Icons.Default.Restaurant,
                        route = "recipes"
                    ),
                    NavigationItem(
                        title = "Profile",
                        icon = Icons.Default.Person,
                        route = "profile"
                    ),
                    NavigationItem(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        route = "settings"
                    )
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel)
            }
            composable("recipes") {
                RecipeScreen()
            }
            composable("profile") {
                ProfileScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}

@Composable
fun RecipeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Recipes Screen",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Coming Soon!",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Profile Screen",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Coming Soon!",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        Text(
            text = "Settings Screen",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Coming Soon!",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
) 