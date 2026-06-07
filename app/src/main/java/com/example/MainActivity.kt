package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CreateScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.ProfileScreen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import com.example.ui.theme.*
import com.example.ui.viewmodel.CreatorViewModel

class MainActivity : ComponentActivity() {
    
    // Instantiate our AndroidViewModel using Kotlin property delegates
    private val viewModel: CreatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup premium Edge-to-Edge display
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val navigationTab by viewModel.navigationTab.collectAsState()
                var currentTab by remember { mutableStateOf(0) }

                LaunchedEffect(navigationTab) {
                    navigationTab?.let { tab ->
                        currentTab = tab
                        viewModel.clearNavigationTab()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        CreatorBottomNavigationBar(
                            selectedTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    },
                    containerColor = DarkBG
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding()) // Respect notch insets comfortably
                    ) {
                        when (currentTab) {
                            0 -> FeedScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            1 -> CreateScreen(
                                viewModel = viewModel,
                                onNavigateBackToFeed = { currentTab = 0 },
                                modifier = Modifier.fillMaxSize()
                            )
                            2 -> AnalyticsScreen(
                                viewModel = viewModel,
                                onNavigateToCreate = { currentTab = 1 },
                                modifier = Modifier.fillMaxSize()
                            )
                            3 -> InboxScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                            4 -> ProfileScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBG)
    ) {
        // Sophisticated, extremely subtle boundary separation lines
        HorizontalDivider(color = GridBorder, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // CRITICAL INSET COMPATIBILITY: Avoid notch & system gesture overlaps
                .height(56.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Feed (Home)
            BottomTabItem(
                isActive = selectedTab == 0,
                activeIcon = Icons.Default.Home,
                inactiveIcon = Icons.Outlined.Home,
                label = "Feed",
                onClick = { onTabSelected(0) },
                tag = "tab_feed"
            )

            // Tab 1: Creator Studio (Plus add)
            BottomTabItem(
                isActive = selectedTab == 1,
                activeIcon = Icons.Default.AddBox,
                inactiveIcon = Icons.Outlined.AddBox,
                label = "Create",
                onClick = { onTabSelected(1) },
                tag = "tab_create",
                isMiddlePlus = true
            )

            // Tab 2: Analytics graphs
            BottomTabItem(
                isActive = selectedTab == 2,
                activeIcon = Icons.Default.Analytics,
                inactiveIcon = Icons.Outlined.Analytics,
                label = "Analytics",
                onClick = { onTabSelected(2) },
                tag = "tab_analytics"
            )

            // Tab 3: Fans Direct Chats
            BottomTabItem(
                isActive = selectedTab == 3,
                activeIcon = Icons.Default.Chat,
                inactiveIcon = Icons.Outlined.Chat,
                label = "Chats",
                onClick = { onTabSelected(3) },
                tag = "tab_chats"
            )

            // Tab 4: Customizable Profile
            BottomTabItem(
                isActive = selectedTab == 4,
                activeIcon = Icons.Default.Person,
                inactiveIcon = Icons.Outlined.Person,
                label = "Profile",
                onClick = { onTabSelected(4) },
                tag = "tab_profile"
            )
        }
    }
}

@Composable
fun RowScope.BottomTabItem(
    isActive: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String,
    isMiddlePlus: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable { onClick() }
            .testTag(tag)
    ) {
        if (isMiddlePlus) {
            // Elegant centered action highlight for the creator's upload plus button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isActive) AccentBlue else CardDark,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activeIcon,
                    contentDescription = label,
                    tint = if (isActive) Color.White else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Icon(
                imageVector = if (isActive) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = if (isActive) AccentBlue else TextSecondary,
                modifier = Modifier.size(26.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(3.dp))
        
        Text(
            text = label,
            color = if (isActive) AccentBlue else TextSecondary,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}
