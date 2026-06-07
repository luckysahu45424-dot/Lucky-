package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CreatorPostImage
import com.example.ui.components.GradientTemplates
import com.example.ui.theme.*
import com.example.ui.viewmodel.CreatorViewModel

@Composable
fun CreateScreen(
    viewModel: CreatorViewModel,
    onNavigateBackToFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Observe form parameters
    val topic by viewModel.createTopic.collectAsState()
    val tone by viewModel.createStyleTone.collectAsState()
    val category by viewModel.createCategory.collectAsState()
    val keywords by viewModel.createKeywords.collectAsState()
    val filterName by viewModel.createFilterName.collectAsState()
    val selectedGradientIdx by viewModel.createSelectedGradientIndex.collectAsState()

    // Observe AI copilot states
    val isGenerating by viewModel.isGeneratingCaption.collectAsState()
    val generatedCaptionTxt by viewModel.generatedCaptionText.collectAsState()
    val aiFeedback by viewModel.aiPostFeedback.collectAsState()
    val editingDraft by viewModel.editingDraftPost.collectAsState()

    val availableTones = listOf("Witty", "Philosophical", "Short & Punchy", "Hype")
    val availableNiches = listOf("Tech", "Travel", "Lifestyle", "Comedy", "Food")
    val availableFilters = listOf("Normal", "Warm", "Cool", "Noir", "Neon")

    // Show success dialog when posted
    var showPostSuccessDialog by remember { mutableStateOf(false) }
    var successType by remember { mutableStateOf("published") }

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Create",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    viewModel.updateTopic("")
                    viewModel.updateKeywords("")
                    viewModel.updateGeneratedCaption("")
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = "Clear form",
                    tint = TextSecondary
                )
            }
        }

        HorizontalDivider(color = GridBorder, thickness = 0.5.dp)

        if (editingDraft != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentBlue.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "⚡ EDITING SAVED DRAFT",
                            color = AccentBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Modifications are persistent to the database draft slot.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    TextButton(
                        onClick = { viewModel.cancelEditingDraft() }
                    ) {
                        Text(
                            text = "Cancel",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // --- Live Aesthetic Preview Card ---
            Text(
                text = "LIVE INSTAGRAM CARD PREVIEW",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(0.5.dp, GridBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Simulated mini header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(InstaPink, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "you_creative",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Instagram Live Preview • $category",
                                color = TextSecondary,
                                fontSize = 8.sp
                            )
                        }
                    }

                    // Simulated live image layout
                    CreatorPostImage(
                        imageUrl = "gradient_$selectedGradientIdx",
                        filterName = filterName,
                        category = category,
                        heightDp = 180
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated caption snippet
                    Text(
                        text = if (generatedCaptionTxt.isNotBlank()) generatedCaptionTxt else "Generate with Gemini to preview caption. Form topic details will compile into this slot.",
                        color = if (generatedCaptionTxt.isNotBlank()) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        maxLines = 3,
                        fontWeight = if (generatedCaptionTxt.isNotBlank()) FontWeight.Normal else FontWeight.Medium
                    )
                }
            }

            // --- Form Inputs ---

            // 1. Topic Idea textfield
            Text(
                text = "1. WHAT IS YOUR PHOTO OR TOPIC TODAY?",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            TextField(
                value = topic,
                onValueChange = { viewModel.updateTopic(it) },
                placeholder = { Text("E.g., Tips to write clean modifier lines in jetpack compose UI grids", color = TextSecondary, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("topic_input_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = AccentBlue,
                    unfocusedIndicatorColor = GridBorder
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // 2. Select Style Tone Capsules list
            Text(
                text = "2. SELECT AI STYLE TONE",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTones.forEach { toneItem ->
                    val isSelected = toneItem == tone
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) AccentBlue else CardDark,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (isSelected) Color.Transparent else GridBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.updateStyleTone(toneItem) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = toneItem,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Select Category capsule list
            Text(
                text = "3. CHOOSE POST CATEGORY NICHE",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableNiches.forEach { catItem ->
                    val isSelected = catItem == category
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) InstaPink else CardDark,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 0.5.dp,
                                color = if (isSelected) Color.Transparent else GridBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.updateCategory(catItem) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = catItem.uppercase(),
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 4. Focus keywords tags
            Text(
                text = "4. SPECIFY FOCUS TAGS (OPTIONAL, COMMA-SEPARATED)",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            TextField(
                value = keywords,
                onValueChange = { viewModel.updateKeywords(it) },
                placeholder = { Text("E.g., jetpack, coding, devlife, minimal", color = TextSecondary, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("keywords_input_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = AccentBlue,
                    unfocusedIndicatorColor = GridBorder
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // 5. Select Backdrop Visual Gradient template strip
            Text(
                text = "5. PICK MOCK PHOTO GRADIENT TEMPLATE",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(GradientTemplates) { index, colors ->
                    val isSelected = selectedGradientIdx == index
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(colors))
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.updateGradientIndex(index) }
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active selection",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            // 6. Select Filter
            Text(
                text = "6. APPLY AESTHETIC FILTER",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableFilters.forEach { filterItem ->
                    val isSelected = filterItem == filterName
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isSelected) AccentBlue else CardDark,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else GridBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.updateFilterName(filterItem) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filterItem,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- AI Copilot Core Trigger ---
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.generateCaptionWithAI()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("ai_generate_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(InstaPurple, InstaPink, InstaOrange, InstaYellow)
                            ),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isGenerating) "AI IS GENERATING..." else "BRAINSTORM WITH GEMINI CO-PILOT",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Loading indicators and custom feedback animation
            if (isGenerating) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = AccentBlue,
                    trackColor = GridBorder
                )
            }

            // --- Caption Edit Area ---
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✍️ INSTAGRAM CAPTION",
                    color = InstaPink,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "EDITABLE",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            TextField(
                value = generatedCaptionTxt,
                onValueChange = { viewModel.updateGeneratedCaption(it) },
                placeholder = { Text("Write your custom caption here or generate with Gemini Co-Pilot...", color = TextSecondary, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("generated_caption_field"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = CardDark,
                    unfocusedContainerColor = CardDark,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedIndicatorColor = InstaPink,
                    unfocusedIndicatorColor = GridBorder
                ),
                shape = RoundedCornerShape(8.dp)
            )

            // Render Co-Pilot Analysis & Tags Tips
            if (aiFeedback.isNotBlank()) {
                Text(
                    text = "🔍 ANALYTICAL COPILOT FEEDS",
                    color = AccentBlue,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardDark, RoundedCornerShape(8.dp))
                        .border(0.5.dp, AccentBlue.copy(0.4f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = aiFeedback,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Saving and Posting action handlers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Draft saver
                Button(
                    onClick = {
                        viewModel.saveDraftOrPublish(isDraft = true) {
                            successType = "drafted"
                            showPostSuccessDialog = true
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_draft_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (editingDraft != null) "UPDATE DRAFT" else "SAVE DRAFT",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Publishers
                Button(
                    onClick = {
                        viewModel.saveDraftOrPublish(isDraft = false) {
                            successType = "published"
                            showPostSuccessDialog = true
                        }
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("publish_post_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "PUBLISH GRADIENT FEED",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }

    // Success dialog
    if (showPostSuccessDialog) {
        val successTitle = if (successType == "drafted") "Draft Saved Successfully!" else "Post Published Successfully!"
        val successBody = if (successType == "drafted") {
            "Your layout draft was compiled in the SQLite Room Database. You can access or publish it anytime from the Drafts section in the Analytics panel!"
        } else {
            "Your beautiful gradient post is now officially live on the mockup Instagram feed! Look out for simulated comments."
        }

        AlertDialog(
            onDismissRequest = {
                showPostSuccessDialog = false
                onNavigateBackToFeed()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = AccentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = successTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    text = successBody,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPostSuccessDialog = false
                        onNavigateBackToFeed()
                    },
                    modifier = Modifier.testTag("success_redirect_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Go to Feed Stories", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            containerColor = CardDark,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
