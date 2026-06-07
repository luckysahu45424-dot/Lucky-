package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Post
import com.example.data.model.FanComment
import com.example.ui.components.CreatorPostImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.CreatorViewModel
import com.example.ui.viewmodel.NotificationItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedScreen(
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.publishedPosts.collectAsState()
    val comments by viewModel.inboxComments.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val followedUsers by viewModel.followedUsers.collectAsState()

    // Screen-level state for showing Creator Tip dialog
    var activeTipTitle by remember { mutableStateOf<String?>(null) }
    var activeTipBody by remember { mutableStateOf<String?>(null) }
    var activeTipUser by remember { mutableStateOf<String?>(null) }

    // Active expanded comment block matching postId
    var expandedPostId by remember { mutableStateOf<Int?>(null) }

    // Real-time custom interactive notification alerts toggle state
    var showNotificationsAlerts by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        // --- Top Instagram/Studio Header Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_flygram_logo_1780794369475),
                        contentDescription = "Flygram Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Column {
                    Text(
                        text = "Flygram",
                        color = AccentBlue,
                        fontFamily = FontFamily.Serif,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "INFLUENCER STUDIO",
                        color = TextSecondary,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = {
                        activeTipTitle = "⚡ Live Feed Signals"
                        activeTipBody = "The algorithm scores content in real-time based on Comment Velocity. Use Gemini in the Inbox/Create tab to build high-relevance hooks!"
                    },
                    modifier = Modifier.testTag("feed_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Feed help tips",
                        tint = TextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .clickable { showNotificationsAlerts = true }
                        .testTag("feed_notifications_bell")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(InstaPink, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }
        }

        HorizontalDivider(color = GridBorder, thickness = 0.5.dp)

        // --- Master list of feed components ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // 1. Stories row containing creator friends offering interactive workflow guides
            item {
                StoriesRow(
                    onTipClicked = { user, title, tip ->
                        activeTipUser = user
                        activeTipTitle = title
                        activeTipBody = tip
                    }
                )
            }

            // Simple divider
            item {
                HorizontalDivider(color = GridBorder, thickness = 0.5.dp)
            }

            // 2. Feed posts
            if (posts.isEmpty()) {
                item {
                    EmptyFeedPlaceholder()
                }
            } else {
                items(posts, key = { it.id }) { post ->
                    val postComments = comments.filter { it.postId == post.id }

                    FeedPostCard(
                        post = post,
                        postComments = postComments,
                        isCommentsExpanded = expandedPostId == post.id,
                        onLikeClicked = { viewModel.likePost(post) },
                        onCommentToggle = {
                            expandedPostId = if (expandedPostId == post.id) null else post.id
                        },
                        onDeletePost = { viewModel.deletePost(post) },
                        isReplyingId = viewModel.isGeneratingReplyId.collectAsState().value,
                        onAiReplyGenerated = { comment ->
                            viewModel.generateSmartReplyForComment(comment, post.caption)
                        },
                        onManualReply = { comment, reply ->
                            viewModel.manuallyReplyToComment(comment, reply)
                        },
                        onLikeComment = { comment ->
                            viewModel.likeComment(comment)
                        },
                        followedUsers = followedUsers,
                        onFollowToggle = { user -> viewModel.toggleFollowUser(user) },
                        onMessageClicked = { user -> viewModel.openChatWithAndNavigate(user) }
                    )
                }
            }
        }
    }

    // --- Elegant interactive tutorial dialog ---
    if (activeTipTitle != null && activeTipBody != null) {
        AlertDialog(
            onDismissRequest = {
                activeTipTitle = null
                activeTipBody = null
                activeTipUser = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = InstaPink,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = activeTipTitle!!,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = activeTipBody!!,
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    
                    if (activeTipUser != null && activeTipUser != "your_story") {
                        val isFollowing = followedUsers.contains(activeTipUser!!)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.toggleFollowUser(activeTipUser!!) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) GridBorder else AccentBlue,
                                    contentColor = if (isFollowing) TextPrimary else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isFollowing) "Following" else "Follow Back", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            IconButton(
                                onClick = {
                                    val userToChat = activeTipUser!!
                                    activeTipTitle = null
                                    activeTipBody = null
                                    activeTipUser = null
                                    viewModel.openChatWithAndNavigate(userToChat)
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(InstaPink.copy(alpha = 0.12f), CircleShape)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "Message", tint = InstaPink, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        activeTipTitle = null
                        activeTipBody = null
                        activeTipUser = null
                    },
                    modifier = Modifier.testTag("tip_close_button")
                ) {
                    Text("Got it!", color = AccentBlue, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = CardDark,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // --- Interactive Alerts Overlay ---
    if (showNotificationsAlerts) {
        AlertDialog(
            onDismissRequest = { showNotificationsAlerts = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CircleNotifications,
                            contentDescription = null,
                            tint = InstaPink,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FlyAlerts Terminal",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    IconButton(onClick = { showNotificationsAlerts = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    Text(
                        text = "REAL-TIME CREATOR ENGAGEMENT FEEDS",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No alerts at the moment.", color = TextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(notifications, key = { it.id }) { alert ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DarkBG),
                                    border = BorderStroke(0.5.dp, GridBorder),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            showNotificationsAlerts = false
                                            viewModel.openChatWithAndNavigate(alert.senderName)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Sender Avatar Initial
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        listOf(InstaPink, InstaOrange)
                                                    ),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = alert.senderAvatarLetter,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = alert.senderName,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.widthIn(max = 110.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                val badgeColor = when (alert.type) {
                                                    "LIKE" -> InstaPink
                                                    "FOLLOW" -> AccentBlue
                                                    else -> InstaOrange
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(badgeColor.copy(0.15f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = alert.type,
                                                        color = badgeColor,
                                                        fontSize = 7.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = alert.detailText,
                                                color = TextSecondary,
                                                fontSize = 10.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Action items row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            val isFollowing = followedUsers.contains(alert.senderName)
                                            Button(
                                                onClick = { 
                                                    viewModel.toggleFollowUser(alert.senderName) 
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isFollowing) GridBorder else AccentBlue
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(
                                                    text = if (isFollowing) "Following" else "Follow",
                                                    color = if (isFollowing) TextPrimary else Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Direct Live Chat Interaction Replies to DM
                                            IconButton(
                                                onClick = {
                                                    showNotificationsAlerts = false
                                                    viewModel.openChatWithAndNavigate(alert.senderName)
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(InstaPink.copy(0.12f), CircleShape)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Chat,
                                                    contentDescription = "Instant DM Chat",
                                                    tint = InstaPink,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            containerColor = CardDark,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun StoriesRow(
    onTipClicked: (String, String, String) -> Unit
) {
    val stories = listOf(
        Triple("your_story", "Add Tip", "Creating stories with high-contrast active gradients triggers 40% more click-through rate relative to plain designs!"),
        Triple("growth_guide", "Metrics", "Focus on 'Saves' and 'Shares'. When a follower saves your post to a collection, the algorithm pushes it higher in Search results!"),
        Triple("luna_captions", "Short is King", "Try producing captions with fewer than 100 characters on food/fashion posts. Let the sunset gradient photo speak!"),
        Triple("alex_travels", "Grid Mastery", "Maintain consistent theme filters -- Warm for travel diaries, Noir for coffee grids. It builds a cohesive creator brand."),
        Triple("tech_copilot", "AI Hooks", "Always start captions with a shocking quote or bold metric to stop the viewer from scrolling past.")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(stories) { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        onTipClicked(
                            item.first,
                            "💡 ${item.second} Masterclass",
                            item.third
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(
                                width = 2.5.dp,
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        InstaOrange,
                                        InstaPink,
                                        InstaPurple,
                                        InstaYellow,
                                        InstaOrange
                                    )
                                ),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CardDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.first.take(2).uppercase(),
                                color = AccentBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (item.first == "your_story") "Your Story" else item.first,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.width(72.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeedPostCard(
    post: Post,
    postComments: List<FanComment>,
    isCommentsExpanded: Boolean,
    onLikeClicked: () -> Unit,
    onCommentToggle: () -> Unit,
    onDeletePost: () -> Unit,
    isReplyingId: Int?,
    onAiReplyGenerated: (FanComment) -> Unit,
    onManualReply: (FanComment, String) -> Unit,
    onLikeComment: (FanComment) -> Unit,
    followedUsers: Set<String>,
    onFollowToggle: (String) -> Unit,
    onMessageClicked: (String) -> Unit
) {
    var isCaptionExpanded by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val dateString = formatter.format(Date(post.timestamp))

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, GridBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark)
                .padding(vertical = 12.dp)
        ) {
            // Post Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(listOf(InstaPink, InstaOrange)),
                                shape = CircleShape
                            )
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = post.category.take(2).uppercase(),
                                color = AccentBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "you_creative",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "$dateString • ${post.category}",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(onClick = onDeletePost) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete creative post",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Post visual representation
            CreatorPostImage(
                imageUrl = post.imageUrl,
                filterName = post.filterName,
                category = post.category,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            // Action Buttons (Like, Comment, Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onLikeClicked,
                        modifier = Modifier.testTag("post_like_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.likes > 412) Icons.Default.Favorite else Icons.BorderWith(Icons.Default.FavoriteBorder),
                            contentDescription = "Like",
                            tint = if (post.likes > 412) ErrorRed else TextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clickable { onCommentToggle() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${postComments.size}",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Share details",
                        tint = TextPrimary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 2.dp)
                    )
                }

                // Save indicator
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save draft preset",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Post metrics
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "${post.likes} likes • ${post.views} views",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Caption details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isCaptionExpanded = !isCaptionExpanded }
                ) {
                    Text(
                        text = buildString {
                            append("you_creative ")
                            append(post.caption)
                        },
                        color = TextPrimary,
                        fontSize = 13.sp,
                        maxLines = if (isCaptionExpanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (post.caption.length > 80 && !isCaptionExpanded) {
                    Text(
                        text = "more",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clickable { isCaptionExpanded = true }
                            .padding(vertical = 2.dp)
                    )
                }

                // AI Co-Pilot Notes badge if present
                if (post.aiCopilotNotes != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardDark, RoundedCornerShape(6.dp))
                            .border(0.5.dp, AccentBlue.copy(0.4f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Notes",
                            tint = AccentBlue,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = post.aiCopilotNotes,
                            color = AccentBlue,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Expandable inline comments sliding viewer
                AnimatedVisibility(
                    visible = isCommentsExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .background(CardDark, RoundedCornerShape(8.dp))
                            .border(0.5.dp, GridBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Follower Interactions (${postComments.size})",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (postComments.isEmpty()) {
                            Text(
                                text = "No comments yet! Publish a draft to test algorithmic follower interactions.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            postComments.forEach { comment ->
                                CommentRow(
                                    comment = comment,
                                    isReplying = isReplyingId == comment.id,
                                    onLikeComment = { onLikeComment(comment) },
                                    onAiReply = { onAiReplyGenerated(comment) },
                                    onManualReply = { replyMsg -> onManualReply(comment, replyMsg) },
                                    followedUsers = followedUsers,
                                    onFollowToggle = onFollowToggle,
                                    onMessageClicked = onMessageClicked
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
fun CommentRow(
    comment: FanComment,
    isReplying: Boolean,
    onLikeComment: () -> Unit,
    onAiReply: () -> Unit,
    onManualReply: (String) -> Unit,
    followedUsers: Set<String>,
    onFollowToggle: (String) -> Unit,
    onMessageClicked: (String) -> Unit
) {
    var showReplyInput by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${comment.authorName}: ",
                    color = AccentBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = comment.commentText,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            IconButton(
                onClick = onLikeComment,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (comment.isLiked) ErrorRed else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Creator Reply block if it exists
        if (comment.isReplied && comment.replyText != null) {
            Row(
                modifier = Modifier
                    .padding(start = 24.dp, top = 4.dp)
                    .background(DarkBG, RoundedCornerShape(4.dp))
                    .padding(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "you_creative (Creator): ",
                    color = InstaPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = comment.replyText,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Reply controllers
        Row(
            modifier = Modifier
                .padding(start = 24.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Reply",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { showReplyInput = !showReplyInput }
                    .padding(vertical = 2.dp)
            )

            // AI Smart Reply Core Action
            Surface(
                onClick = onAiReply,
                color = Color.Transparent,
                enabled = !isReplying
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI smart response",
                        tint = AccentBlue,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isReplying) "Drafting..." else "AI Smart Reply",
                        color = AccentBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Interactive Follow toggle text anchor
            val isFollowing = followedUsers.contains(comment.authorName)
            Text(
                text = if (isFollowing) "Following" else "Follow",
                color = if (isFollowing) InstaPink else AccentBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onFollowToggle(comment.authorName) }
                    .padding(vertical = 2.dp)
            )

            // Direct Message text anchor
            Text(
                text = "Message",
                color = InstaOrange,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onMessageClicked(comment.authorName) }
                    .padding(vertical = 2.dp)
            )
        }

        AnimatedVisibility(visible = showReplyInput) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Write physical reply...", fontSize = 11.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("comment_reply_field"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkBG,
                        unfocusedContainerColor = DarkBG,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onManualReply(replyText)
                            replyText = ""
                            showReplyInput = false
                        }
                    },
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("comment_reply_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text("Send", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyFeedPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp)
            .background(CardDark, RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.PartyMode,
            contentDescription = null,
            tint = InstaPink,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your feed is empty!",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Navigate to the Create tab (Plus icon) to generate smart captions with Gemini, draft photos, and build follower engagement instantly!",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// Visual indicator trick helper for borders in Compose
fun Icons.BorderWith(vector: androidx.compose.ui.graphics.vector.ImageVector): androidx.compose.ui.graphics.vector.ImageVector = vector
