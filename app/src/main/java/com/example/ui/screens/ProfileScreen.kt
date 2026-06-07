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
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Post
import com.example.ui.components.CreatorPostImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.CreatorViewModel

@Composable
fun ProfileScreen(
    viewModel: CreatorViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.publishedPosts.collectAsState()
    val name by viewModel.profileName.collectAsState()
    val handle by viewModel.profileHandle.collectAsState()
    val bio by viewModel.profileBio.collectAsState()
    val avatarIndex by viewModel.profileAvatarColorIndex.collectAsState()
    val avatarCostume by viewModel.profileAvatarCostume.collectAsState()
    val avatarPhotoUri by viewModel.profileAvatarPhotoUri.collectAsState()
    val followers by viewModel.profileFollowers.collectAsState()
    val following by viewModel.profileFollowing.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var selectedPostDetails by remember { mutableStateOf<Post?>(null) }

    val avatarGradients = listOf(
        listOf(InstaPurple, AccentBlue),
        listOf(InstaPink, InstaOrange),
        listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
        listOf(Color(0xFF11998e), Color(0xFF38ef7d))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBG)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    text = "flygram/$handle",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 200.dp)
                )
            }

            IconButton(
                onClick = {
                    viewModel.addNotification(
                        type = "FOLLOW",
                        senderName = "system",
                        detailText = "Logged in session fully verified."
                    )
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.PowerSettingsNew,
                    contentDescription = "Session",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(color = GridBorder, thickness = 0.5.dp)

        // --- Profile Cards Body ---
        Box(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Main stats block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Customizable Costume Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(avatarGradients.getOrElse(avatarIndex) { avatarGradients[0] }),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                            .testTag("profile_avatar_box")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CardDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!avatarPhotoUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = avatarPhotoUri,
                                    contentDescription = "User Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else if (avatarCostume.isNotEmpty()) {
                                Text(
                                    text = avatarCostume,
                                    fontSize = 32.sp
                                )
                            } else {
                                Text(
                                    text = name.take(2).uppercase(),
                                    color = AccentBlue,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }

                    // Scoreboard grid
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ProfileStatItem(countString = "${posts.size}", label = "Posts")
                        ProfileStatItem(countString = formatStatCount(followers), label = "Followers")
                        ProfileStatItem(countString = "$following", label = "Following")
                    }
                }

                // Name & Bio Display Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(0.5.dp, GridBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = name,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bio,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Call to actions buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("edit_profile_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.incrementFollowers() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("boost_followers_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(listOf(InstaPurple, InstaPink, InstaOrange)),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Promote Profile", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Grid of Published items
                Text(
                    text = "PUBLISHED DRAFTS GRID (${posts.size} items)",
                    color = InstaPink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (posts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(CardDark, RoundedCornerShape(16.dp))
                            .border(0.5.dp, GridBorder, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No content published yet!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(posts) { post ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPostDetails = post }
                                    .testTag("profile_published_grid_item_${post.id}")
                            ) {
                                // Draw creative mockup box
                                CreatorPostImage(
                                    imageUrl = post.imageUrl ?: "gradient_0",
                                    filterName = post.filterName,
                                    category = post.category,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(0.3f))
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(4.dp),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(CardDark.copy(0.85f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = post.category,
                                            color = TextPrimary,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                        Text(
                                            text = "${post.likes}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expanded Post Details Modal Dialog
            selectedPostDetails?.let { post ->
                AlertDialog(
                    onDismissRequest = { selectedPostDetails = null },
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Published Content Details",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { selectedPostDetails = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    text = {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                CreatorPostImage(
                                    imageUrl = post.imageUrl ?: "gradient_0",
                                    filterName = post.filterName,
                                    category = post.category,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .background(CardDark.copy(0.8f), RoundedCornerShape(4.dp))
                                        .align(Alignment.TopEnd)
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(post.filterName, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = post.caption,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                MiniMetricBox(icon = Icons.Default.Favorite, label = "Likes", value = "${post.likes}")
                                MiniMetricBox(icon = Icons.Default.Visibility, label = "Views", value = "${post.views}")
                                MiniMetricBox(icon = Icons.Default.Tag, label = "Topic", value = post.category)
                            }
                        }
                    },
                    confirmButton = {},
                    containerColor = CardDark,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // Inline Custom Edit Dialog
            if (isEditing) {
                var editName by remember { mutableStateOf(name) }
                var editHandle by remember { mutableStateOf(handle) }
                var editBio by remember { mutableStateOf(bio) }
                var editAvatarIndex by remember { mutableStateOf(avatarIndex) }
                var editCostume by remember { mutableStateOf(avatarCostume) }
                var editPhotoUri by remember { mutableStateOf<String?>(avatarPhotoUri) }

                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    if (uri != null) {
                        editPhotoUri = uri.toString()
                    }
                }

                val costumeOptions = listOf(
                    "🚀" to "Astronaut",
                    "👾" to "Retro Gamer",
                    "🎸" to "Indie Rocker",
                    "🐼" to "Chill Panda",
                    "🦄" to "Mystic Unicorn",
                    "🍕" to "Gourmet Chef",
                    "🦊" to "Clever Fox",
                    "👑" to "Royal Influencer",
                    "🧙" to "Coder Wizard",
                    "🐱" to "Ninja Neko",
                    "🐯" to "Artist Tiger"
                )

                AlertDialog(
                    onDismissRequest = { isEditing = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = InstaPink)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Customize Flygram Profile",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextField(
                                value = editName,
                                onValueChange = { editName = it },
                                label = { Text("Display Name", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_name"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkBG,
                                    unfocusedContainerColor = DarkBG,
                                    focusedIndicatorColor = AccentBlue
                                )
                            )

                            TextField(
                                value = editHandle,
                                onValueChange = { editHandle = it },
                                label = { Text("Influencer Handle", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_handle"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkBG,
                                    unfocusedContainerColor = DarkBG,
                                    focusedIndicatorColor = AccentBlue
                                )
                            )

                            TextField(
                                value = editBio,
                                onValueChange = { editBio = it },
                                label = { Text("Biography Bio", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_bio"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkBG,
                                    unfocusedContainerColor = DarkBG,
                                    focusedIndicatorColor = AccentBlue
                                )
                            )

                            Text(
                                text = "Profile Photo / Image (Optional)",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkBG, RoundedCornerShape(12.dp))
                                    .border(1.dp, GridBorder, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(CardDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!editPhotoUri.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = editPhotoUri,
                                            contentDescription = "Preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(32.dp).testTag("choose_photo_btn")
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text("Choose Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (!editPhotoUri.isNullOrEmpty()) {
                                        TextButton(
                                            onClick = { editPhotoUri = null },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(20.dp).testTag("clear_photo_btn")
                                        ) {
                                            Text("Remove Photo / Use Avatar", color = Color.Red, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            TextField(
                                value = editPhotoUri ?: "",
                                onValueChange = { editPhotoUri = it.ifEmpty { null } },
                                label = { Text("Or Paste Image URL directly", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_photo_url_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkBG,
                                    unfocusedContainerColor = DarkBG,
                                    focusedIndicatorColor = AccentBlue
                                )
                            )

                            Text(
                                text = "Active Avatar Border Gradient",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                avatarGradients.forEachIndexed { index, colors ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Brush.linearGradient(colors), CircleShape)
                                            .clickable { editAvatarIndex = index }
                                            .border(
                                                width = if (editAvatarIndex == index) 3.dp else 0.dp,
                                                color = if (editAvatarIndex == index) TextPrimary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                    )
                                }
                            }

                            Text(
                                text = "Choose Costume Profile Persona / Character",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(costumeOptions) { costumeOpt ->
                                    val isSelected = editCostume == costumeOpt.first
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) AccentBlue.copy(0.15f) else CardDark)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) AccentBlue else GridBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                             .clickable { editCostume = costumeOpt.first }
                                             .padding(horizontal = 14.dp, vertical = 8.dp)
                                     ) {
                                         Text(text = costumeOpt.first, fontSize = 22.sp)
                                         Spacer(modifier = Modifier.height(4.dp))
                                         Text(
                                             text = costumeOpt.second,
                                            color = if (isSelected) AccentBlue else TextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            TextField(
                                value = editCostume,
                                onValueChange = { if (it.length <= 4) editCostume = it },
                                label = { Text("Or Type Custom Profile Character/Emoji", fontSize = 11.sp) },
                                modifier = Modifier.fillMaxWidth().testTag("edit_profile_costume_custom_input"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = DarkBG,
                                    unfocusedContainerColor = DarkBG,
                                    focusedIndicatorColor = AccentBlue
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.updateProfile(editName, editHandle, editBio, editAvatarIndex, editCostume, editPhotoUri)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                        ) {
                            Text("Save Modifications", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { isEditing = false }) {
                            Text("Dismiss", color = TextSecondary)
                        }
                    },
                    containerColor = CardDark,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(countString: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = countString,
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MiniMetricBox(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(
        modifier = Modifier
            .background(DarkBG, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TextSecondary, fontSize = 9.sp)
        Text(text = value, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

private fun formatStatCount(count: Int): String {
    return if (count >= 1000) {
        val thousand = count / 1000
        val remainder = (count % 1000) / 100
        "$thousand.${remainder}K"
    } else {
        "$count"
    }
}
