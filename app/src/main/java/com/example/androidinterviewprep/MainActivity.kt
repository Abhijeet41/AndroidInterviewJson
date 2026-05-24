package com.example.androidinterviewprep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidinterviewprep.data.repository.DataSource
import com.example.androidinterviewprep.theme.MyApplicationTheme
import com.example.androidinterviewprep.ui.detail.QuestionDetailScreen
import com.example.androidinterviewprep.ui.home.HomeScreen
import com.example.androidinterviewprep.ui.list.QuestionListScreen
import com.example.androidinterviewprep.ui.quiz.QuizScreen
import com.example.androidinterviewprep.ui.search.SearchScreen
import com.example.androidinterviewprep.viewmodel.QuestionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Quiz : Screen("quiz", "Quiz", Icons.Default.Quiz)
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    val viewModel: QuestionViewModel = hiltViewModel()
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val bottomNavigationItems = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Quiz
    )
    
    val showBottomBar = currentRoute in bottomNavigationItems.map { it.route }
    val snackbarHostState = remember { SnackbarHostState() }
    val dataSource by viewModel.dataSource.collectAsState()

    // Notify the user if they are using offline cached data or asset fallback
    LaunchedEffect(dataSource) {
        if (dataSource == DataSource.CACHE) {
            snackbarHostState.showSnackbar(
                message = "Offline: Using cached questions.",
                duration = SnackbarDuration.Short
            )
        } else if (dataSource == DataSource.ASSETS) {
            snackbarHostState.showSnackbar(
                message = "Offline: Using bundled fallback questions.",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavigationItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onCategoryClick = { categoryName ->
                        navController.navigate("list/$categoryName")
                    }
                )
            }
            
            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onQuestionClick = { categoryName, questionId ->
                        navController.navigate("detail/$categoryName/$questionId")
                    }
                )
            }
            
            composable(Screen.Quiz.route) {
                QuizScreen(
                    viewModel = viewModel
                )
            }
            
            composable(
                route = "list/{categoryName}",
                arguments = listOf(
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                QuestionListScreen(
                    categoryName = categoryName,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onQuestionClick = { questionId ->
                        navController.navigate("detail/$categoryName/$questionId")
                    }
                )
            }
            
            composable(
                route = "detail/{categoryName}/{questionId}",
                arguments = listOf(
                    navArgument("categoryName") { type = NavType.StringType },
                    navArgument("questionId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                val questionId = backStackEntry.arguments?.getInt("questionId") ?: 0
                QuestionDetailScreen(
                    categoryName = categoryName,
                    questionId = questionId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToQuestion = { nextQuestionId ->
                        // Navigate to detail of same category, but different question
                        navController.navigate("detail/$categoryName/$nextQuestionId") {
                            // Pop current detail screen to prevent infinite backstack accumulation when pressing Next/Prev
                            popUpTo("detail/{categoryName}/{questionId}") {
                                inclusive = true
                            }
                        }
                    }
                )
            }
        }
    }
}
