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
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FanComment
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.CreatorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InboxScreen(
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    val comments by viewModel.inboxComments.collectAsState()
    val chatRooms by viewModel.chatRooms.collectAsState()
    val activeChatUser by viewModel.currentActiveChatUser.collectAsState()
    val followedUsers by viewModel.followedUsers.collectAsState()
    
    // Filter out comments that belong to general inbox (postId = 0)
    val inboxComments = comments.filter { it.postId == 0 }

    val unrepliedCount = inboxComments.count { !it.isReplied }
    val isReplyingId by viewModel.isGeneratingReplyId.collectAsState()

    // Search query state
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        if (activeChatUser != null) {
            // Render Live Conversation Chat Box
            LiveChatBoxView(
                user = activeChatUser!!,
                messages = chatRooms[activeChatUser] ?: emptyList(),
                onBackClicked = { viewModel.closeChat() },
                onSendMessage = { text -> viewModel.sendChatMessage(activeChatUser!!, text) },
                followedUsers = followedUsers,
                onFollowToggle = { user -> viewModel.toggleFollowUser(user) }
            )
        } else {
            // Render Chats List Screen
            
            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
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
                    Text(
                        text = "Flygram Chats",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // High-end Search Bar Integration
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search chats, contacts, or messages...",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search icon",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("inbox_search_bar"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = AccentBlue,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtered active rooms based on user query
            val filteredChatRooms = if (searchQuery.isEmpty()) {
                chatRooms
            } else {
                chatRooms.filter { (sender, msgs) ->
                    sender.contains(searchQuery, ignoreCase = true) ||
                    msgs.any { it.text.contains(searchQuery, ignoreCase = true) }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Online/Active Contacts horizontal row slider
                item {
                    Column(modifier = Modifier.padding(bottom = 14.dp, top = 4.dp)) {
                        Text(
                            text = "Active Contacts",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                            letterSpacing = 0.5.sp
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            val contacts = listOf("brand_collab_hub", "aspiring_creator_x", "sam_designs", "pixel_perfect", "luna_explores")
                            items(contacts) { contact ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { viewModel.openChatWith(contact) }
                                        .testTag("active_contact_quick_$contact")
                                ) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(
                                                    brush = Brush.linearGradient(
                                                        listOf(InstaPurple, InstaPink, InstaOrange)
                                                    ),
                                                    shape = CircleShape
                                                )
                                                .padding(2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(DarkBG, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = contact.take(2).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                        // Active green dot
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(Color(0xFF38EF7D), CircleShape)
                                                .border(2.dp, DarkBG, CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = contact.take(10) + if (contact.length > 10) ".." else "",
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Encrypted enclave card description helper
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(0.5.dp, GridBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            viewModel.addNotification("FOLLOW", "collab_hub", "opened the marketing portals!")
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(InstaPink.copy(0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = InstaPink, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Encrypted Messaging Enclave", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Direct client chats are safely contained inside this sandbox block.", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }

                if (chatRooms.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No active direct messenger dialogues yet.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else if (filteredChatRooms.isEmpty() && searchQuery.isNotEmpty()) {
                    item {
                        NoSearchResultsPlaceholder(query = searchQuery, onClear = { searchQuery = "" })
                    }
                } else {
                    item {
                        Text(
                            text = "Recent Messages",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                            letterSpacing = 0.5.sp
                        )
                    }

                    items(filteredChatRooms.keys.toList()) { sender ->
                        val msgs = filteredChatRooms[sender] ?: emptyList()
                        val lastMsg = msgs.lastOrNull()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            border = BorderStroke(0.5.dp, GridBorder),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openChatWith(sender) }
                                .testTag("chat_room_item_$sender")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Room icon avatar
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                listOf(InstaPurple, InstaPink)
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sender.take(2).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "@$sender",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        val formattedTime = lastMsg?.timestamp?.let {
                                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it))
                                        } ?: ""
                                        Text(
                                            text = formattedTime,
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = lastMsg?.text ?: "No messages in thread.",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    val isFollowing = followedUsers.contains(sender)
                                    Button(
                                        onClick = { viewModel.toggleFollowUser(sender) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isFollowing) GridBorder else AccentBlue,
                                            contentColor = if (isFollowing) TextPrimary else Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(
                                            text = if (isFollowing) "Following" else "Follow",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.openChatWith(sender) },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(InstaPink.copy(alpha = 0.12f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = "Message",
                                            tint = InstaPink,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun LiveChatBoxView(
    user: String,
    messages: List<ChatMessage>,
    onBackClicked: () -> Unit,
    onSendMessage: (String) -> Unit,
    followedUsers: Set<String>,
    onFollowToggle: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        // Chat Top Navbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        brush = Brush.linearGradient(listOf(InstaPurple, InstaPink)),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "@$user",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF38EF7D), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sender is Online",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            val isFollowing = followedUsers.contains(user)
            Button(
                onClick = { onFollowToggle(user) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) GridBorder else AccentBlue,
                    contentColor = if (isFollowing) TextPrimary else Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(30.dp).padding(end = 4.dp)
            ) {
                Text(if (isFollowing) "Following" else "Follow", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        HorizontalDivider(color = GridBorder, thickness = 0.5.dp)

        // Scrollable Messages Box Pane
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            reverseLayout = false
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(0.5.dp, GridBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sandbox PM Sandbox Box",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "This message box simulates active client and fan conversions. AI Copilot handles instant, style-oriented rapid drafts below.",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            items(messages, key = { it.id }) { msg ->
                val alignment = if (msg.isFromMe) Alignment.End else Alignment.Start
                val bubbleBg = if (msg.isFromMe) AccentBlue else CardDark
                val textColor = if (msg.isFromMe) Color.White else TextPrimary
                val corners = if (msg.isFromMe) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                } else {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = alignment
                ) {
                    Box(
                        modifier = Modifier
                            .background(bubbleBg, corners)
                            .border(
                                width = if (msg.isFromMe) 0.dp else 0.5.dp,
                                color = if (msg.isFromMe) Color.Transparent else GridBorder,
                                shape = corners
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .widthIn(max = 260.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = textColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(msg.timestamp))
                    Text(
                        text = if (msg.isFromMe) "Sent • $formattedTime" else "@$user • $formattedTime",
                        color = TextSecondary,
                        fontSize = 8.sp,
                        modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
                    )
                }
            }
        }

        // Gemini AI Draft Assistance Tool Chips
        Surface(
            color = CardDark,
            border = BorderStroke(0.5.dp, GridBorder),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gemini Pilot Suggestions Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(InstaPink, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🤖 AI COPILOT DRAFT CHIPS",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "TAP CHIP TO LOAD DRAFT",
                        color = InstaOrange,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // AI Draft Chip item
                val customAIDraft = when (user) {
                    "brand_collab_hub" -> "Hi! Thanks for reaching out. Yes, I'd absolutely love to collaborate. We can schedule a video call to discuss your campaign deliverables. I can send over our latest Flygram engagement scorecard as well!"
                    "aspiring_creator_x" -> "Hi! Consistently writing human captions comes from practicing hooks early. Focus on clear breaks, relatable stories, and using our custom Style Tones inside Flygram!"
                    else -> "Thanks for checking in! The gradient blocks are crafted directly using Jetpack Compose canvas styling. Let me know if you want the snippet!"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBG, RoundedCornerShape(12.dp))
                        .border(BorderStroke(0.5.dp, GridBorder), RoundedCornerShape(12.dp))
                        .clickable { replyText = customAIDraft }
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini AI Helper",
                            tint = InstaPink,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Optimized Professional Draft Response",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = customAIDraft,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                // Send Input Text Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("Write message here...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkBG,
                            unfocusedContainerColor = DarkBG,
                            focusedIndicatorColor = AccentBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = {
                            if (replyText.isNotBlank()) {
                                onSendMessage(replyText)
                                replyText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .size(42.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send PM message",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngagementScorecard(unrepliedCount: Int, totalCount: Int) {
    val completionPercentage = if (totalCount > 0) {
        ((totalCount - unrepliedCount).toFloat() / totalCount * 100).toInt()
    } else {
        100
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(0.5.dp, AccentBlue.copy(0.3f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(listOf(InstaPink, InstaOrange)),
                        shape = CircleShape
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$completionPercentage%",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (unrepliedCount > 0) "Boost Community Trust Score!" else "Excellent Engagement Rate!",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = if (unrepliedCount > 0) {
                        "Answering follower concerns within 1 hour boosts algorithmic page authority by up to 22%."
                    } else {
                        "You are fully responsive to fan threads! Your organic community feedback metrics are flourishing."
                    },
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun InboxCommentCard(
    comment: FanComment,
    isAIGenerating: Boolean,
    onLikeClicked: () -> Unit,
    onDraftWithAI: () -> Unit,
    onPostReply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("h:mm a • MMM d", Locale.getDefault()) }
    val dateString = formatter.format(Date(comment.timestamp))

    var isManualEditActive by remember { mutableStateOf(false) }
    var editedReplyText by remember { mutableStateOf("") }

    // Synchronize editing field state when AI generates a draft
    LaunchedEffect(comment.replyText) {
        if (comment.replyText != null) {
            editedReplyText = comment.replyText
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(0.5.dp, if (comment.isReplied) GridBorder else InstaPink.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header row with mock author avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = if (comment.authorName.contains("hub")) InstaOrange else AccentBlue,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = comment.authorName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.authorName,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            if (comment.authorName.contains("hub") || comment.authorName.contains("brand")) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified sponsor collab",
                                    tint = AccentBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = dateString,
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onLikeClicked) {
                        Icon(
                            imageVector = if (comment.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Heart",
                            tint = if (comment.isLiked) ErrorRed else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Dismiss",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Follower comment text details
            Text(
                text = comment.commentText,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // AI and action blocks
            if (!comment.isReplied && !isAIGenerating && !isManualEditActive) {
                // Unreplied - display raw action suggestions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Manual reply trigger button
                    Button(
                        onClick = { isManualEditActive = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                        border = BorderStroke(1.dp, GridBorder),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.weight(1f).height(38.dp)
                    ) {
                        Text("QUICK MANUAL", color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Direct AI Generator Button
                    Button(
                        onClick = onDraftWithAI,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.weight(1.3f).height(38.dp).testTag("inbox_ai_reply_btn_${comment.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(InstaPurple, InstaPink, InstaOrange)),
                                    RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DRAFT WITH AI", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            } else if (isAIGenerating) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        text = "🤖 AI Copilot is tailoring a smart dialogue...",
                        color = AccentBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = AccentBlue, trackColor = DarkBG)
                }
            } else {
                // Replied or in active manual/AI Draft refine state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkBG, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (comment.isReplied && !isManualEditActive) "✔️ ACTIVE SENT RESPONSE" else "✍️ PROPOSED RESPONSE DRAFT",
                            color = if (comment.isReplied && !isManualEditActive) Color.Green else InstaPink,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        if (!comment.isReplied) {
                            Text(
                                text = "AI FORMULATION",
                                color = TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    if (comment.isReplied && !isManualEditActive) {
                        // Confirmed reply
                        Text(
                            text = comment.replyText ?: "",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap to alter reply details",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isManualEditActive = true }
                        )
                    } else {
                        // Editable Draft state
                        TextField(
                            value = editedReplyText,
                            onValueChange = { editedReplyText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("inbox_reply_edit_field"),
                            textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = CardDark,
                                unfocusedContainerColor = CardDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedIndicatorColor = AccentBlue,
                                unfocusedIndicatorColor = GridBorder
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    isManualEditActive = false
                                    editedReplyText = comment.replyText ?: ""
                                }
                            ) {
                                Text("Cancel", color = TextSecondary, fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (editedReplyText.isNotBlank()) {
                                        onPostReply(editedReplyText)
                                        isManualEditActive = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("inbox_approve_reply_btn"),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SEND & APPROVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyInboxPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp)
            .background(CardDark, RoundedCornerShape(24.dp))
            .border(1.dp, GridBorder, RoundedCornerShape(24.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MarkChatRead,
            contentDescription = null,
            tint = Color.Green,
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Your Fan Inbox is cleared!",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "All brand inquiries and fan comment questions have been successfully sorted. Great work keeping the community fully engaged today!",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun NoSearchResultsPlaceholder(query: String, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .background(CardDark, RoundedCornerShape(20.dp))
            .border(0.5.dp, GridBorder, RoundedCornerShape(20.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = InstaPink,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No results for \"$query\"",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Check the username spelling or try searching for another conversational keyword.",
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        TextButton(onClick = onClear) {
            Text("Clear Search Query", color = AccentBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
