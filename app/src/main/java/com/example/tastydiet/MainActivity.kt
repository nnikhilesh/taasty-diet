package com.example.tastydiet

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavController
import com.example.tastydiet.ui.*
import com.example.tastydiet.ui.screens.*
import com.example.tastydiet.ui.components.HomeDashboard
import com.example.tastydiet.data.RecipeManager
import com.example.tastydiet.ui.theme.TastyDietTheme
import com.example.tastydiet.viewmodel.*
import com.example.tastydiet.ui.components.FloatingBubble
import com.example.tastydiet.ui.components.AIAssistantPopup
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

// Enhanced TabItem with selected/unselected icons
data class TabItem(
    val title: String, 
    val selectedIcon: ImageVector, 
    val unselectedIcon: ImageVector
)

enum class NotificationType {
    SUCCESS, ERROR, INFO, WARNING
}

@Composable
fun ElegantNotification(
    message: String,
    type: NotificationType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        NotificationType.SUCCESS -> Color(0xFF4CAF50)
        NotificationType.ERROR -> Color(0xFFF44336)
        NotificationType.INFO -> Color(0xFF2196F3)
        NotificationType.WARNING -> Color(0xFFFF9800)
    }
    
    val icon = when (type) {
        NotificationType.SUCCESS -> Icons.Default.CheckCircle
        NotificationType.ERROR -> Icons.Default.Error
        NotificationType.INFO -> Icons.Default.Info
        NotificationType.WARNING -> Icons.Default.Warning
    }
    
    val offsetY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 300),
        label = "notification_offset"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1000f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .offset(y = offsetY.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// Screen Content Functions
@Composable
fun HomeScreenContent(navController: NavController) {
    val context = LocalContext.current
    val profileViewModel = remember {
        ProfileViewModel(context.applicationContext as Application)
    }
    val recipeViewModel = remember {
        RecipeViewModel(context.applicationContext as Application)
    }
    val enhancedFoodLogViewModel = remember {
        EnhancedFoodLogViewModel(context.applicationContext as Application)
    }

    val profiles by profileViewModel.profiles.collectAsState(emptyList())
    val recipes by recipeViewModel.recipes.collectAsState(emptyList())
    val currentFoodLogs by enhancedFoodLogViewModel.currentFoodLogs.collectAsState(emptyList())
    val foodSuggestions by enhancedFoodLogViewModel.foodSuggestions.collectAsState(emptyList())
    val currentTotals by enhancedFoodLogViewModel.currentTotals.collectAsState(
        initial = EnhancedFoodLogViewModel.TodayTotals()
    )

    LaunchedEffect(Unit) {
        if (profiles.isNotEmpty() && enhancedFoodLogViewModel.selectedProfileId.value == null) {
            val firstProfile = profiles.first()
            enhancedFoodLogViewModel.setSelectedProfile(firstProfile.id)
        }
    }

    HomeDashboard(
        profiles = profiles,
        recentRecipes = recipes.take(5),
        recentFoods = emptyList(),
        nutritionalSuggestions = foodSuggestions,
        consumedCalories = currentTotals.calories,
        consumedProtein = currentTotals.protein,
        consumedCarbs = currentTotals.carbs,
        consumedFat = currentTotals.fat,
        consumedFiber = currentTotals.fiber,
        foodLogs = currentFoodLogs,
        onProfileClick = { profile ->
            enhancedFoodLogViewModel.setSelectedProfile(profile.id)
        },
        onRecipeClick = { recipe ->
            navController.navigate("recipe_detail/${recipe.id}")
        },
        onFoodLogged = { foodName, quantity, unit, mealType, profileId, selectedDate ->
            enhancedFoodLogViewModel.addFoodLogWithNutritionalData(foodName, quantity, unit, mealType, profileId, selectedDate)
        },
        onRecipeLogged = { recipe, quantity, profileId ->
            enhancedFoodLogViewModel.addFoodLogWithNutritionalData(recipe.name, quantity, "g", "Lunch", profileId, java.time.LocalDate.now())
        },
        onSearchQuery = { query ->
            enhancedFoodLogViewModel.searchFood(query)
        },
        onViewAllRecipesClick = {
            navController.navigate("recipes")
        },
        onFoodLogUpdated = { foodLog ->
            enhancedFoodLogViewModel.updateFoodLog(foodLog)
        },
        onFoodLogDeleted = { foodLog ->
            enhancedFoodLogViewModel.deleteFoodLog(foodLog)
        },
        onDateSelected = { date ->
            enhancedFoodLogViewModel.setSelectedDate(date)
        }
    )
}

@Composable
fun RecipesScreenContent(navController: NavController) {
    val context = LocalContext.current
    val recipeViewModel = remember {
        RecipeViewModel(context.applicationContext as Application)
    }
    val profileViewModel = remember {
        ProfileViewModel(context.applicationContext as Application)
    }
    val enhancedFoodLogViewModel = remember {
        EnhancedFoodLogViewModel(context.applicationContext as Application)
    }
    val aiAssistantViewModel = remember {
        AIAssistantViewModel(context.applicationContext as Application)
    }
    
    RecipesScreen(
        recipeViewModel = recipeViewModel,
        profileViewModel = profileViewModel,
        foodLogViewModel = enhancedFoodLogViewModel,
        aiAssistantViewModel = aiAssistantViewModel
    )
}

@Composable
fun InventoryScreenContent() {
    val context = LocalContext.current
    val inventoryViewModel = remember {
        InventoryViewModel(context.applicationContext as Application)
    }
    val shoppingListViewModel = remember {
        ShoppingListViewModel(context.applicationContext as Application)
    }
    
    var showShoppingList by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showShoppingList = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showShoppingList) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Icon(Icons.Default.Inventory, contentDescription = "Inventory", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inventory")
                }
                
                Button(
                    onClick = { showShoppingList = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showShoppingList) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping List", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shopping List")
                }
            }
        }
        
        if (showShoppingList) {
            ShoppingListScreen(shoppingListViewModel = shoppingListViewModel)
        } else {
            InventoryScreen(viewModel = inventoryViewModel)
        }
    }
}

@Composable
fun ProfileScreenContent(_navController: NavController) {
    val context = LocalContext.current
    val profileViewModel = remember {
        ProfileViewModel(context.applicationContext as Application)
    }
    
    ProfileScreen(
        viewModel = profileViewModel
    )
}

@Composable
fun SettingsScreenContent(onNavigateToEditProfile: () -> Unit) {
    val settingsViewModel = remember {
        SettingsViewModel()
    }
    
    SettingsScreen(
        onNavigateToEditProfile = onNavigateToEditProfile,
        settingsViewModel = settingsViewModel
    )
}

@Composable
fun AddRecipeScreenContent(navController: NavController) {
    val context = LocalContext.current
    val recipeManager = remember {
        RecipeManager(context.applicationContext)
    }
    
    AddRecipeScreen(
        recipeManager = recipeManager,
        onRecipeAdded = {
            navController.popBackStack()
        },
        onNavigateBack = {
            navController.popBackStack()
        }
    )
}

@Composable
fun RecipeDetailScreenContent(navController: NavController, recipeId: Int) {
    val context = LocalContext.current
    val recipeViewModel = remember {
        RecipeViewModel(context.applicationContext as Application)
    }
    
    val recipes by recipeViewModel.recipes.collectAsState(emptyList())
    val recipe = recipes.find { it.id == recipeId }
    
    if (recipe != null) {
        RecipeDetailScreen(
            recipe = recipe,
            onNavigateBack = {
                navController.popBackStack()
            }
        )
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recipe not found")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TastyDietApp() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }
    
    // AI Assistant state
    val context = LocalContext.current
    val aiAssistantViewModel = remember {
        AIAssistantViewModel(context.applicationContext as Application)
    }
    
    var showAIAssistant by remember { mutableStateOf(false) }
    
    val messages by aiAssistantViewModel.messages.collectAsStateWithLifecycle()
    val isLoading by aiAssistantViewModel.isLoading.collectAsStateWithLifecycle()
    val isRecording by aiAssistantViewModel.isRecording.collectAsStateWithLifecycle()
    
    // Vosk state
    val showLanguagePicker by aiAssistantViewModel.showLanguagePicker.collectAsStateWithLifecycle()
    val recognizedText by aiAssistantViewModel.recognizedText.collectAsStateWithLifecycle()
    val isVoskListening by aiAssistantViewModel.isVoskListening.collectAsStateWithLifecycle()
    val currentLanguage by aiAssistantViewModel.currentLanguage.collectAsStateWithLifecycle()
    val availableLanguages = aiAssistantViewModel.getAvailableLanguages()
    
    // Video player state
    val showVideoPlayer by aiAssistantViewModel.showVideoPlayer.collectAsStateWithLifecycle()
    val currentVideoRecipe by aiAssistantViewModel.currentVideoRecipe.collectAsStateWithLifecycle()
    val currentVideoId by aiAssistantViewModel.currentVideoId.collectAsStateWithLifecycle()
    
    // Simplified 4-tab structure with beautiful Material 3 icons
    val tabs = listOf(
        TabItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Recipes", Icons.Filled.Restaurant, Icons.Outlined.Restaurant),
        TabItem("Inventory", Icons.Filled.Inventory, Icons.Outlined.Inventory),
        TabItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main app content
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Tasty Diet",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                navController.navigate("settings") {
                                    launchSingleTop = true
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon, 
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    tab.title,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            },
                            selected = selectedTab == index,
                            onClick = { 
                                selectedTab = index
                                val route = when (tab.title) {
                                    "Home" -> "home"
                                    "Recipes" -> "recipes"
                                    "Inventory" -> "inventory"
                                    "Profile" -> "profile"
                                    else -> tab.title.lowercase()
                                }
                                navController.navigate(route) {
                                    popUpTo(navController.graph.id) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
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
                    HomeScreenContent(navController)
                }
                
                composable("recipes") {
                    RecipesScreenContent(navController)
                }
                
                composable("inventory") {
                    InventoryScreenContent()
                }
                
                composable("profile") {
                    ProfileScreenContent(navController)
                }
                
                composable("settings") {
                    SettingsScreenContent(
                        onNavigateToEditProfile = {
                            navController.navigate("edit_profile")
                        }
                    )
                }
                composable("edit_profile") {
                    ProfileScreenContent(navController)
                }
                composable("add_recipe") {
                    AddRecipeScreenContent(navController)
                }
                composable("recipe_detail/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull() ?: 0
                    RecipeDetailScreenContent(navController, recipeId)
                }
            }
        }
        
        // Floating bubble overlay - positioned above everything
        FloatingBubble(
            onBubbleClick = { showAIAssistant = true }
        )
        
        // AI Assistant popup
        AIAssistantPopup(
            isVisible = showAIAssistant,
            onDismiss = { showAIAssistant = false },
            messages = messages,
            onSendMessage = { message ->
                aiAssistantViewModel.sendMessage(message)
            },
            onVoiceRecord = {
                aiAssistantViewModel.toggleVoiceRecording()
            },
            isRecording = isRecording,
            isLoading = isLoading,
            showLanguagePicker = showLanguagePicker,
            onLanguageSelected = { language ->
                aiAssistantViewModel.startVoiceRecognition(language)
            },
            onHideLanguagePicker = {
                aiAssistantViewModel.hideLanguagePicker()
            },
            availableLanguages = availableLanguages,
            currentLanguage = currentLanguage,
            recognizedText = recognizedText,
            isVoskListening = isVoskListening,
            showVideoPlayer = showVideoPlayer,
            currentVideoRecipe = currentVideoRecipe,
            currentVideoId = currentVideoId,
            onVideoDismiss = {
                aiAssistantViewModel.hideRecipeVideo()
            },
            onVideoLinkClick = { url ->
                aiAssistantViewModel.handleVideoLinkClick(url)
            }
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Run AI Model Test in background
        runAIModelTest()
        
        setContent {
            TastyDietTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TastyDietApp()
                }
            }
        }
    }
    
    private fun runAIModelTest() {
        // Run test after a short delay to ensure app is fully loaded
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            try {
                val tester = com.example.tastydiet.test.AIModelTester(this)
                tester.runCompleteTest()
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error running AI test: ${e.message}")
            }
        }, 3000) // 3 second delay
    }
}