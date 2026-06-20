package ch.juliusbaer.daybreak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ch.juliusbaer.daybreak.data.MockData
import ch.juliusbaer.daybreak.ui.components.Avatar
import ch.juliusbaer.daybreak.ui.navigation.Routes
import ch.juliusbaer.daybreak.ui.screens.ArticleScreen
import ch.juliusbaer.daybreak.ui.screens.CardScreen
import ch.juliusbaer.daybreak.ui.screens.ConciergeScreen
import ch.juliusbaer.daybreak.ui.screens.DaybreakScreen
import ch.juliusbaer.daybreak.ui.screens.InsightsScreen
import ch.juliusbaer.daybreak.ui.screens.MoveScreen
import ch.juliusbaer.daybreak.ui.screens.ProfileScreen
import ch.juliusbaer.daybreak.ui.screens.SendMoneyScreen
import ch.juliusbaer.daybreak.ui.screens.WealthScreen
import ch.juliusbaer.daybreak.ui.theme.Hairline
import ch.juliusbaer.daybreak.ui.theme.Ink
import ch.juliusbaer.daybreak.ui.theme.InkTertiary
import ch.juliusbaer.daybreak.ui.theme.JuliusBaerTheme
import ch.juliusbaer.daybreak.ui.theme.Paper
import ch.juliusbaer.daybreak.ui.theme.SurfaceWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JuliusBaerTheme { JbApp() }
        }
    }
}

enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Daybreak(Routes.DAYBREAK, "Daybreak", Icons.Outlined.WbSunny),
    Wealth(Routes.WEALTH, "Wealth", Icons.Outlined.PieChart),
    Move(Routes.MOVE, "Move", Icons.Outlined.SwapHoriz),
    Concierge(Routes.CONCIERGE, "Concierge", Icons.Outlined.SupportAgent)
}

@Composable
fun JbApp() {
    val nav = rememberNavController()
    val backEntry by nav.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route
    val isTopLevel = route in Routes.topLevel

    Scaffold(
        containerColor = Paper,
        topBar = {
            JbTopBar(
                showBack = route != null && !isTopLevel,
                title = titleFor(route),
                onBack = { nav.popBackStack() },
                onProfile = { nav.navigate(Routes.PROFILE) }
            )
        },
        bottomBar = {
            if (isTopLevel) BottomNav(route) { nav.switchTab(it) }
        }
    ) { padding: PaddingValues ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = nav, startDestination = Routes.DAYBREAK) {

                composable(Routes.DAYBREAK) {
                    DaybreakScreen(onNavigate = { target -> nav.go(target) })
                }
                composable(Routes.WEALTH) { WealthScreen() }
                composable(Routes.MOVE) {
                    MoveScreen(onSend = { nav.navigate(Routes.SEND) })
                }
                composable(Routes.CONCIERGE) { ConciergeScreen() }

                composable(Routes.SEND) {
                    SendMoneyScreen(onBack = { nav.popBackStack() })
                }
                composable(Routes.CARD) { CardScreen() }
                composable(Routes.INSIGHTS) {
                    InsightsScreen(onOpenArticle = { idx -> nav.navigate(Routes.article(idx)) })
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(onSignOut = { nav.popBackStack() })
                }
                composable(
                    route = Routes.ARTICLE_PATTERN,
                    arguments = listOf(navArgument("idx") { type = NavType.IntType })
                ) { entry ->
                    val idx = entry.arguments?.getInt("idx") ?: 0
                    val article = MockData.insights.getOrElse(idx) { MockData.insights[0] }
                    ArticleScreen(
                        article = article,
                        onDiscuss = { nav.switchTab(Routes.CONCIERGE) }
                    )
                }
            }
        }
    }
}

/** Navigate, switching tab cleanly for top-level routes and pushing for details. */
private fun NavHostController.go(target: String) {
    if (target in Routes.topLevel) switchTab(target) else navigate(target)
}

private fun NavHostController.switchTab(target: String) {
    navigate(target) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun titleFor(route: String?): String = when (route) {
    Routes.SEND -> "Send money"
    Routes.CARD -> "Card"
    Routes.INSIGHTS -> "Insights"
    Routes.PROFILE -> "Profile"
    Routes.ARTICLE_PATTERN -> "Insight"
    else -> ""
}

@Composable
private fun JbTopBar(
    showBack: Boolean,
    title: String,
    onBack: () -> Unit,
    onProfile: () -> Unit
) {
    Column {
        Box(Modifier.fillMaxWidth().height(48.dp)) {
            if (showBack) {
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .size(24.dp)
                        .clickable { onBack() }
                )
                Text(title, color = Ink, fontSize = 15.sp, modifier = Modifier.align(Alignment.Center))
            } else {
                Text(
                    "Julius Bär",
                    color = Ink,
                    fontSize = 14.sp,
                    letterSpacing = 2.5.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                Box(Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                    Avatar(MockData.clientInitials, size = 30.dp, onClick = onProfile)
                }
            }
        }
        HorizontalDivider(color = Hairline, thickness = 0.7.dp)
    }
}

@Composable
private fun BottomNav(currentRoute: String?, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = SurfaceWhite, tonalElevation = 0.dp) {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onSelect(tab.route) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, fontSize = 10.5.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Ink,
                    selectedTextColor = Ink,
                    unselectedIconColor = InkTertiary,
                    unselectedTextColor = InkTertiary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
