package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Publish
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Post
import com.example.ui.components.CreatorPostImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.CreatorViewModel

@Composable
fun AnalyticsScreen(
    viewModel: CreatorViewModel,
    onNavigateToCreate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val drafts by viewModel.draftPosts.collectAsState()
    val posts by viewModel.publishedPosts.collectAsState()

    // Mock analytics statistics compiled instantly
    val totalFollowers = 24820
    val followerGrowth = "+1.4k this week"
    val avgEngagement = "8.42%"
    val totalViews = posts.sumOf { it.views }
    val totalLikes = posts.sumOf { it.likes }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Analytics,
                contentDescription = null,
                tint = InstaPink,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Analytics & Drafts Hub",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = GridBorder, thickness = 0.5.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- Creator Performance Metrics Panel ---
            item {
                Column {
                    Text(
                        text = "CORE INFLUENCER METRICS",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Fans",
                            value = "24.8K",
                            trend = followerGrowth,
                            isTrendPositive = true,
                            icon = Icons.Default.People,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Engagement",
                            value = avgEngagement,
                            trend = "+0.8% organic",
                            isTrendPositive = true,
                            icon = Icons.Default.TrendingUp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Live Views",
                            value = if (totalViews > 0) "${(totalViews / 1000.0).let { "%.1f".format(it) }}K" else "8.6K",
                            trend = "Simulated reach",
                            isTrendPositive = true,
                            icon = Icons.Default.Visibility,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Feed Likes",
                            value = if (totalLikes > 0) "$totalLikes" else "1.5K",
                            trend = "Likes saved",
                            isTrendPositive = true,
                            icon = Icons.Default.Favorite,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // --- Custom Canvas Analytics Chart ---
            item {
                Column {
                    Text(
                        text = "WEEKLY IMPRESSIONS PROFILE (REACH)",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(0.5.dp, GridBorder),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "14.2K total impressions",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "LIVE GRAPH",
                                    color = AccentBlue,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom painted Canvas Line Chart
                            ImpressionsCanvasChart()

                            Spacer(modifier = Modifier.height(12.dp))

                            // X-Axis Labels row
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                weekdays.forEach { day ->
                                    Text(
                                        text = day,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // --- Niche breakdown visual progress bars ---
            item {
                Column {
                    Text(
                        text = "ORGANIC REACH BY CATEGORY",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(0.5.dp, GridBorder),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NicheProgressBar(nicheName = "Travel", reachCount = "52K views", progress = 0.85f, barColor = InstaPink)
                            NicheProgressBar(nicheName = "Tech (AI/Workflow)", reachCount = "44K views", progress = 0.72f, barColor = AccentBlue)
                            NicheProgressBar(nicheName = "Lifestyle", reachCount = "20K views", progress = 0.38f, barColor = InstaOrange)
                            NicheProgressBar(nicheName = "Comedy/Food", reachCount = "8K views", progress = 0.15f, barColor = InstaYellow)
                        }
                    }
                }
            }

            // --- Saved Drafts Database list ---
            item {
                Column {
                    Text(
                        text = "SAVED DRAFTS IN ROOM DATABASE (${drafts.size})",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    if (drafts.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            border = BorderStroke(0.5.dp, GridBorder),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "No drafts in database",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "When you choose 'Save Draft' inside the Creation tab, your post model is fully saved locally here.",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // Display direct scrollable list or rows of drafts
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(drafts, key = { it.id }) { draft ->
                                DraftItemCard(
                                    draft = draft,
                                    onPublish = { viewModel.publishExistingDraft(draft) },
                                    onEdit = {
                                        viewModel.startEditingDraft(draft)
                                        onNavigateToCreate()
                                    },
                                    onDelete = { viewModel.deletePost(draft) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    trend: String,
    isTrendPositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(0.5.dp, GridBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Icon(imageVector = icon, contentDescription = null, tint = InstaPink, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = trend,
                color = if (isTrendPositive) AccentBlue else TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ImpressionsCanvasChart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(Color.Transparent)
    ) {
        val width = size.width
        val height = size.height

        // Mock coordinate points over 7 days
        val dataPoints = listOf(0.2f, 0.4f, 0.35f, 0.75f, 0.55f, 0.9f, 0.8f)

        val spacing = width / (dataPoints.size - 1)
        val path = Path()
        val fillPath = Path()

        // Starting point calculations
        val startY = height - (dataPoints[0] * height * 0.8f)
        path.moveTo(0f, startY)
        fillPath.moveTo(0f, height)
        fillPath.lineTo(0f, startY)

        for (i in 1 until dataPoints.size) {
            val pointX = i * spacing
            val pointY = height - (dataPoints[i] * height * 0.8f)
            path.lineTo(pointX, pointY)
            fillPath.lineTo(pointX, pointY)
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw ambient background area fill gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    InstaPink.copy(alpha = 0.35f),
                    InstaOrange.copy(alpha = 0.05f),
                    Color.Transparent
                )
            )
        )

        // Draw crisp neon line path
        drawPath(
            path = path,
            color = InstaPink,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw dot indicators and data tags
        for (i in dataPoints.indices) {
            val cx = i * spacing
            val cy = height - (dataPoints[i] * height * 0.8f)

            // Outer pulse circle
            drawCircle(
                color = AccentBlue.copy(0.4f),
                radius = 7.dp.toPx(),
                center = Offset(cx, cy)
            )

            // Inner focus dot
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
fun NicheProgressBar(
    nicheName: String,
    reachCount: String,
    progress: Float,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = nicheName, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(text = reachCount, color = TextSecondary, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color(0xFFE8DEF8), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(
                        Brush.linearGradient(listOf(barColor, barColor.copy(alpha = 0.6f))),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun DraftItemCard(
    draft: Post,
    onPublish: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkBG),
        border = BorderStroke(0.5.dp, GridBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(220.dp)
            .padding(vertical = 4.dp)
            .clickable { onEdit() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Visual header representation
            CreatorPostImage(
                imageUrl = draft.imageUrl,
                filterName = draft.filterName,
                category = draft.category,
                heightDp = 100
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = draft.caption,
                color = TextPrimary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                maxLines = 2,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Action triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete draft",
                        tint = ErrorRed,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit draft",
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onPublish,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("draft_publish_button_${draft.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Publish,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "PUBLISH", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
