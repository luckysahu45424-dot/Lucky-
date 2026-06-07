package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Post
import com.example.data.model.FanComment
import com.example.data.repository.CreatorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreatorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CreatorRepository
    
    // --- Live Data Streams ---
    val publishedPosts: StateFlow<List<Post>>
    val draftPosts: StateFlow<List<Post>>
    val inboxComments: StateFlow<List<FanComment>>

    // --- State Managers for Creating Form ---
    private val _createTopic = MutableStateFlow("")
    val createTopic = _createTopic.asStateFlow()

    private val _createStyleTone = MutableStateFlow("Witty")
    val createStyleTone = _createStyleTone.asStateFlow()

    private val _createCategory = MutableStateFlow("Tech")
    val createCategory = _createCategory.asStateFlow()

    private val _createKeywords = MutableStateFlow("")
    val createKeywords = _createKeywords.asStateFlow()

    private val _createFilterName = MutableStateFlow("Normal")
    val createFilterName = _createFilterName.asStateFlow()

    // Interactive custom visual gradient selection for the mockup image background
    private val _createSelectedGradientIndex = MutableStateFlow(0)
    val createSelectedGradientIndex = _createSelectedGradientIndex.asStateFlow()

    // --- Active Draft Editing States ---
    private val _editingDraftPost = MutableStateFlow<Post?>(null)
    val editingDraftPost = _editingDraftPost.asStateFlow()

    // --- AI Generator States ---
    private val _isGeneratingCaption = MutableStateFlow(false)
    val isGeneratingCaption = _isGeneratingCaption.asStateFlow()

    private val _generatedCaptionText = MutableStateFlow("")
    val generatedCaptionText = _generatedCaptionText.asStateFlow()

    private val _aiPostFeedback = MutableStateFlow("")
    val aiPostFeedback = _aiPostFeedback.asStateFlow()

    private val _isGeneratingReplyId = MutableStateFlow<Int?>(null) // Tracks comment ID currently being replied to
    val isGeneratingReplyId = _isGeneratingReplyId.asStateFlow()

    // --- Dynamic User Profile Customization States ---
    private val _profileName = MutableStateFlow("Aryan Sahu")
    val profileName = _profileName.asStateFlow()

    private val _profileHandle = MutableStateFlow("flygram_creator")
    val profileHandle = _profileHandle.asStateFlow()

    private val _profileBio = MutableStateFlow("Visual designer, code crafter & coffee connoisseur. Building pixel-perfect micro-studios using Jetpack Compose ☕🎨")
    val profileBio = _profileBio.asStateFlow()

    private val _profileAvatarColorIndex = MutableStateFlow(0)
    val profileAvatarColorIndex = _profileAvatarColorIndex.asStateFlow()

    private val _profileAvatarCostume = MutableStateFlow("🚀")
    val profileAvatarCostume = _profileAvatarCostume.asStateFlow()

    private val _profileAvatarPhotoUri = MutableStateFlow<String?>(null)
    val profileAvatarPhotoUri = _profileAvatarPhotoUri.asStateFlow()

    private val _profileFollowers = MutableStateFlow(24850)
    val profileFollowers = _profileFollowers.asStateFlow()

    private val _profileFollowing = MutableStateFlow(342)
    val profileFollowing = _profileFollowing.asStateFlow()

    private val _followedUsers = MutableStateFlow<Set<String>>(setOf("sam_designs"))
    val followedUsers = _followedUsers.asStateFlow()

    // --- Interactive Notifications States ---
    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(1, "FOLLOW", "sam_designs", "S", "started following you", System.currentTimeMillis() - 300000), // 5m ago
            NotificationItem(2, "LIKE", "pixel_perfect", "P", "liked your Spacing Secrets draft post", System.currentTimeMillis() - 720000), // 12m ago
            NotificationItem(3, "COMMENT", "brand_collab_hub", "B", "mentioned you in a branding proposal", System.currentTimeMillis() - 1800000), // 30m ago
            NotificationItem(4, "FOLLOW", "luna_explores", "L", "started following you", System.currentTimeMillis() - 7200000) // 2h ago
        )
    )
    val notifications = _notifications.asStateFlow()

    // --- Direct Messaging (DM) Message Box States ---
    private val _chatRooms = MutableStateFlow<Map<String, List<ChatMessage>>>(
        mapOf(
            "brand_collab_hub" to listOf(
                ChatMessage(1, "brand_collab_hub", "Hey there! Love your distinct aesthetic and creative focus. We have an upcoming campaign and would love to collaborate on a sponsored post. Do you have a media kit we can check out?", System.currentTimeMillis() - 3600000, false)
            ),
            "aspiring_creator_x" to listOf(
                ChatMessage(1, "aspiring_creator_x", "Hello! I am just starting out as a content creator and I struggle so much with caption ideas. How do you consistently write captions that feel so human?", System.currentTimeMillis() - 7200000, false)
            ),
            "graphic_gurus" to listOf(
                ChatMessage(1, "graphic_gurus", "Your gradient choices are legendary! Do you design your assets directly on mobile, or do you export from vector studios?", System.currentTimeMillis() - 10800000, false),
                ChatMessage(2, "Me", "Thank you! I design my mockup blocks using dynamic Jetpack Compose Canvas and custom gradients, which allows fluid, fully native UI rendering. I do most planning right inside Flygram!", System.currentTimeMillis() - 10200000, true)
            )
        )
    )
    val chatRooms = _chatRooms.asStateFlow()

    private val _currentActiveChatUser = MutableStateFlow<String?>(null)
    val currentActiveChatUser = _currentActiveChatUser.asStateFlow()

    private val _navigationTab = MutableStateFlow<Int?>(null)
    val navigationTab = _navigationTab.asStateFlow()

    fun navigateToTab(tabIndex: Int) {
        _navigationTab.value = tabIndex
    }

    fun clearNavigationTab() {
        _navigationTab.value = null
    }

    fun openChatWithAndNavigate(user: String) {
        openChatWith(user)
        navigateToTab(3)
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CreatorRepository(database.creatorDao())

        // Stream mapping
        publishedPosts = repository.publishedPosts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        draftPosts = repository.draftPosts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        inboxComments = repository.inboxComments
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Automatically pre-populate database if empty on launch
        viewModelScope.launch {
            repository.publishedPosts.first().let { currentList ->
                if (currentList.isEmpty()) {
                    populateInitialDemoData()
                }
            }
        }
    }

    // --- Form setters ---
    fun updateTopic(value: String) { _createTopic.value = value }
    fun updateStyleTone(value: String) { _createStyleTone.value = value }
    fun updateCategory(value: String) { _createCategory.value = value }
    fun updateKeywords(value: String) { _createKeywords.value = value }
    fun updateFilterName(value: String) { _createFilterName.value = value }
    fun updateGradientIndex(index: Int) { _createSelectedGradientIndex.value = index }
    fun updateGeneratedCaption(text: String) { _generatedCaptionText.value = text }

    fun startEditingDraft(post: Post) {
        _editingDraftPost.value = post
        _createTopic.value = ""
        _createKeywords.value = ""
        _generatedCaptionText.value = post.caption
        _createFilterName.value = post.filterName
        _createCategory.value = post.category
        val index = post.imageUrl?.removePrefix("gradient_")?.toIntOrNull() ?: 0
        _createSelectedGradientIndex.value = index
        _aiPostFeedback.value = post.aiCopilotNotes ?: ""
    }

    fun cancelEditingDraft() {
        _editingDraftPost.value = null
        _createTopic.value = ""
        _createKeywords.value = ""
        _generatedCaptionText.value = ""
        _aiPostFeedback.value = ""
        _createSelectedGradientIndex.value = (0..5).random()
    }

    // --- AI Copilot Core Logic ---

    fun generateCaptionWithAI() {
        val topic = _createTopic.value
        val tone = _createStyleTone.value
        val keywords = _createKeywords.value
        val category = _createCategory.value

        if (topic.isBlank()) {
            _generatedCaptionText.value = "⚠️ Please enter a brief topic or idea first to guide our AI Creative Captain!"
            return
        }

        viewModelScope.launch {
            _isGeneratingCaption.value = true
            _generatedCaptionText.value = "✍️ AI Copilot is brainstorming hooks and trends for you..."
            _aiPostFeedback.value = "⚡ Analyzing market parameters for $category..."

            val generated = repository.generateCaption(topic, tone, keywords)
            _generatedCaptionText.value = generated

            // After generating caption, automatically run our visual feedback algorithm
            val feedback = repository.generatePostFeedback(generated, category)
            _aiPostFeedback.value = feedback
            _isGeneratingCaption.value = false
        }
    }

    fun generateSmartReplyForComment(comment: FanComment, postCaption: String) {
        viewModelScope.launch {
            _isGeneratingReplyId.value = comment.id
            val smartReply = repository.generateSmartReply(comment.commentText, postCaption)
            
            // Save the drafted smart response directly back to the database as draft reply
            val updatedComment = comment.copy(
                replyText = smartReply,
                isReplied = true
            )
            repository.updateComment(updatedComment)
            _isGeneratingReplyId.value = null
        }
    }

    // --- Database CRUD Actions ---

    fun saveDraftOrPublish(isDraft: Boolean, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val finalCaption = _generatedCaptionText.value.ifBlank { 
                "Creating daily magic. 📸" 
            }
            
            // Build the image string using the selected gradient template
            val mockImageString = "gradient_${_createSelectedGradientIndex.value}"

            val editingPost = _editingDraftPost.value
            val finalPost = if (editingPost != null) {
                editingPost.copy(
                    caption = finalCaption,
                    imageUrl = mockImageString,
                    filterName = _createFilterName.value,
                    likes = if (isDraft) 0 else (100..450).random(),
                    views = if (isDraft) 0 else (1200..3500).random(),
                    category = _createCategory.value,
                    isDraft = isDraft,
                    aiCopilotNotes = if (_aiPostFeedback.value.isNotBlank()) _aiPostFeedback.value else null,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                Post(
                    caption = finalCaption,
                    imageUrl = mockImageString,
                    filterName = _createFilterName.value,
                    likes = if (isDraft) 0 else (100..450).random(),
                    views = if (isDraft) 0 else (1200..3500).random(),
                    commentsCount = if (isDraft) 0 else (5..20).random(),
                    category = _createCategory.value,
                    isDraft = isDraft,
                    aiCopilotNotes = if (_aiPostFeedback.value.isNotBlank()) _aiPostFeedback.value else null
                )
            }

            val postId = if (editingPost != null) {
                repository.updatePost(finalPost)
                editingPost.id.toLong()
            } else {
                repository.insertPost(finalPost)
            }

            // If we publish, insert 2 or 3 funny mock fan questions for this particular post!
            if (!isDraft) {
                val mockNames = listOf("Emma_design", "tech_lead_pro", "travel_vibe_99", "alex_creatives", "sophie_art")
                val mockComments = when (_createCategory.value) {
                    "Tech" -> listOf(
                        "Wow, this is an incredible workflow! What tools did you use to set this up? 💻",
                        "Great thoughts on design systems. Do you think we still need traditional designers?"
                    )
                    "Travel" -> listOf(
                        "This looks absolutely serene! Adding this to my bucket list immediately! ✈️",
                        "Wait, what camera filter did you use? The colors are vibrant!"
                    )
                    else -> listOf(
                        "This is such an engaging post. Truly appreciate the creative vibes! ✨",
                        "Absolutely spot on! Loved the hook!"
                    )
                }

                mockNames.shuffled().take(2).forEachIndexed { index, author ->
                    repository.insertComment(
                        FanComment(
                            postId = postId.toInt(),
                            authorName = author,
                            commentText = mockComments.getOrElse(index) { "Incredible work! 👏" }
                        )
                    )
                }
            }

            // Clear create states
            _createTopic.value = ""
            _createKeywords.value = ""
            _generatedCaptionText.value = ""
            _aiPostFeedback.value = ""
            _createSelectedGradientIndex.value = (0..5).random()
            _editingDraftPost.value = null

            onComplete()
        }
    }

    fun publishExistingDraft(post: Post) {
        viewModelScope.launch {
            val updated = post.copy(
                isDraft = false,
                likes = (150..600).random(),
                views = (1800..5000).random(),
                timestamp = System.currentTimeMillis()
            )
            repository.updatePost(updated)
            
            // Insert initial simulated comment
            repository.insertComment(
                FanComment(
                    postId = post.id,
                    authorName = "creatives_united",
                    commentText = "So glad you published this! Excellent content as always. ✨"
                )
            )
        }
    }

    fun likePost(post: Post) {
        viewModelScope.launch {
            val updated = post.copy(likes = post.likes + 1)
            repository.updatePost(updated)
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            repository.deletePost(post)
        }
    }

    fun likeComment(comment: FanComment) {
        viewModelScope.launch {
            val updated = comment.copy(isLiked = !comment.isLiked)
            repository.updateComment(updated)
        }
    }

    fun manuallyReplyToComment(comment: FanComment, replyText: String) {
        viewModelScope.launch {
            val updated = comment.copy(
                replyText = replyText,
                isReplied = true
            )
            repository.updateComment(updated)
        }
    }

    fun deleteComment(comment: FanComment) {
        viewModelScope.launch {
            repository.deleteComment(comment)
        }
    }

    // --- Dynamic Profile Operations ---
    fun updateProfile(name: String, handle: String, bio: String, avatarIndex: Int, costume: String, customPhotoUri: String? = null) {
        _profileName.value = name
        _profileHandle.value = handle
        _profileBio.value = bio
        _profileAvatarColorIndex.value = avatarIndex
        _profileAvatarCostume.value = costume
        _profileAvatarPhotoUri.value = customPhotoUri
        
        // Generate an instant system notification
        addNotification("FOLLOW", "system", "Successfully updated your Flygram creator profile cards!")
    }

    fun incrementFollowers() {
        _profileFollowers.value += 1
        addNotification("FOLLOW", "organic_fan", "A brand new reader started following you from the explore grids!")
    }

    fun toggleFollowUser(username: String) {
        val currentSet = _followedUsers.value.toMutableSet()
        val isNowFollowing = if (currentSet.contains(username)) {
            currentSet.remove(username)
            _profileFollowing.value = maxOf(0, _profileFollowing.value - 1)
            false
        } else {
            currentSet.add(username)
            _profileFollowing.value += 1
            true
        }
        _followedUsers.value = currentSet

        // Update corresponding notifications mapping if any
        _notifications.value = _notifications.value.map {
            if (it.senderName == username && it.type == "FOLLOW") {
                it.copy(isInteracted = isNowFollowing)
            } else {
                it
            }
        }
    }

    // --- Notifications Actions ---
    fun toggleNotificationInteraction(id: Int) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) {
                val nextState = !it.isInteracted
                if (it.type == "FOLLOW") {
                    val currentSet = _followedUsers.value.toMutableSet()
                    if (nextState) {
                        currentSet.add(it.senderName)
                    } else {
                        currentSet.remove(it.senderName)
                    }
                    _followedUsers.value = currentSet
                    _profileFollowing.value = if (nextState) _profileFollowing.value + 1 else maxOf(0, _profileFollowing.value - 1)
                }
                it.copy(isInteracted = nextState)
            } else {
                it
            }
        }
    }

    fun addNotification(type: String, senderName: String, detailText: String) {
        val newItem = NotificationItem(
            id = (notifications.value.maxOfOrNull { it.id } ?: 0) + 1,
            type = type,
            senderName = senderName,
            senderAvatarLetter = senderName.take(1).uppercase(),
            detailText = detailText,
            timestamp = System.currentTimeMillis()
        )
        _notifications.value = listOf(newItem) + _notifications.value
    }

    // --- Message Box Actions ---
    fun openChatWith(user: String) {
        if (!_chatRooms.value.containsKey(user)) {
            _chatRooms.value = _chatRooms.value.toMutableMap().apply {
                put(user, listOf(
                    ChatMessage(
                        id = 1,
                        senderName = user,
                        text = "Hey! Let's chat here in Flygram Message Box.",
                        timestamp = System.currentTimeMillis() - 60000,
                        isFromMe = false
                    )
                ))
            }
        }
        _currentActiveChatUser.value = user
    }

    fun closeChat() {
        _currentActiveChatUser.value = null
    }

    fun sendChatMessage(user: String, text: String) {
        if (text.isBlank()) return
        val currentMessages = _chatRooms.value[user] ?: emptyList()
        val nextMessageId = (currentMessages.maxOfOrNull { it.id } ?: 0) + 1
        val myMsg = ChatMessage(nextMessageId, "Me", text, System.currentTimeMillis(), true)
        
        val updatedList = currentMessages + myMsg
        _chatRooms.value = _chatRooms.value.toMutableMap().apply {
            put(user, updatedList)
        }

        // Trigger simulated follow-up response
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            simulateReplyFrom(user)
        }
    }

    private fun simulateReplyFrom(user: String) {
        val replies = when (user) {
            "brand_collab_hub" -> listOf(
                "That sounds amazing! Let's schedule a Google Meet next Tuesday if you are free. 📈",
                "Our marketing division is reviewing our campaign templates right now. We can offer $550 per promotional draft!",
                "Fantastic! Could you send over your weekly demographic stats from the Flygram Analytics dashboard?"
            )
            "aspiring_creator_x" -> listOf(
                "Oh that's exceptionally helpful! I will try utilizing the Style Tone selector in Flygram to optimize my hooks.",
                "Wow! I didn't know the comment velocity has such a strong impact on organic reach. Thanks for sharing raw tips!",
                "I'm going to bookmark this chat thread. Truly a lifesaver tutorial!"
            )
            else -> listOf(
                "Amazing! I'll tell our design team to test out Flygram's warm theme gradient pairings as well.",
                "Thanks for the outstanding support! Loving this community workspace.",
                "Makes perfect sense. Keep up the legendary designs!"
            )
        }
        
        val currentMessages = chatRooms.value[user] ?: emptyList()
        val nextMessageId = (currentMessages.maxOfOrNull { it.id } ?: 0) + 1
        val replyText = replies.random()
        val replyMsg = ChatMessage(nextMessageId, user, replyText, System.currentTimeMillis(), false)
        
        _chatRooms.value = _chatRooms.value.toMutableMap().apply {
            put(user, currentMessages + replyMsg)
        }
        
        // Add follower notification that they sent you a direct message!
        addNotification("COMMENT", user, "sent you a DM: \"$replyText\"")
    }

    // --- Demo Data Initializer ---

    private suspend fun populateInitialDemoData() {
        // 1. Initial Feed Posts (already published)
        val post1Id = repository.insertPost(
            Post(
                caption = "The secrets to building a pixel-perfect dark theme. It's not about black backgrounds; it's about layering sophisticated shades of slate (0xFF121212), generous spacing, and colorful, energetic gradients that draw the reader's eye, just like this interactive mockup feed page! Design and draft your posts in the Create tab and generate hashtags instantly with Gemini.\n\nWhich visual aesthetic do you prefer? Minimal glassmorphism or high-contrast brutalist? Let me know! 👇",
                imageUrl = "gradient_0",
                filterName = "Normal",
                likes = 412,
                views = 2840,
                commentsCount = 3,
                category = "Tech",
                timestamp = System.currentTimeMillis() - 3600000 * 4, // 4 hours ago
                isDraft = false,
                aiCopilotNotes = "🎉 High engagement potential! Caption structure hits multiple hooks cleanly. Best shared in tech morning blocks."
            )
        )

        val post2Id = repository.insertPost(
            Post(
                caption = "Wandering through the quiet cobblestone alleys of Venice during sunrise. The silence here is completely absolute, broken only by the occasional splashing water of a modern gondola. Travel forces you to appreciate slow creations rather than instant results, finding stories in physical drafts.\n\nSaving this as a lifestyle template block. Where should the next voyage take us? 🗺️✈️",
                imageUrl = "gradient_1",
                filterName = "Warm",
                likes = 852,
                views = 4210,
                commentsCount = 2,
                category = "Travel",
                timestamp = System.currentTimeMillis() - 3600000 * 24, // 1 day ago
                isDraft = false,
                aiCopilotNotes = "💡 Tip: Include scenic travel emojis and double carriage breaks to boost long-scroll readability parameters."
            )
        )

        val post3Id = repository.insertPost(
            Post(
                caption = "Unpopular opinion: A crisp morning espresso, crafted perfectly in a ceramic cup, outperforms any complex coffee drink. Simple, punchy, beautiful. No modifiers. No extra syrups. Just pure creative fuel. ☕⚡\n\nHow do you kickstart your writing flow?",
                imageUrl = "gradient_2",
                filterName = "Noir",
                likes = 310,
                views = 1560,
                commentsCount = 2,
                category = "Lifestyle",
                timestamp = System.currentTimeMillis() - 3600000 * 48, // 2 days ago
                isDraft = false,
                aiCopilotNotes = "✨ Quick Hype: Coffee content drives highly relatable organic comments. Pair this with interactive micro Q&A stories!"
            )
        )

        // 2. Insert Comments & DMs associated with the posts above
        // Post 1 comments
        repository.insertComment(
            FanComment(
                postId = post1Id.toInt(),
                authorName = "creative_designer",
                commentText = "Agreed! Absolute spacing and custom layers always look much cleaner than generic shadows. 🔥",
                timestamp = System.currentTimeMillis() - 3000000
            )
        )
        repository.insertComment(
            FanComment(
                postId = post1Id.toInt(),
                authorName = "techGuy_32",
                commentText = "Can you share the hex color values for that slate design? Looks incredibly premium!",
                timestamp = System.currentTimeMillis() - 2500000
            )
        )
        repository.insertComment(
            FanComment(
                postId = post1Id.toInt(),
                authorName = "influencer_academy",
                commentText = "The hooks in this caption are so clean! Bookmarked for style references.",
                timestamp = System.currentTimeMillis() - 1500000
            )
        )

        // Post 2 comments
        repository.insertComment(
            FanComment(
                postId = post2Id.toInt(),
                authorName = "nomad_journey",
                commentText = "Venice is timeless! Try visiting the local artisan workshops in Murano too! 🛶",
                timestamp = System.currentTimeMillis() - 3600000 * 20
            )
        )
        repository.insertComment(
            FanComment(
                postId = post2Id.toInt(),
                authorName = "photo_enthusiast",
                commentText = "That sunrise lighting is magnificent! Which camera lens did you use?",
                timestamp = System.currentTimeMillis() - 3600000 * 18
            )
        )

        // Post 3 comments
        repository.insertComment(
            FanComment(
                postId = post3Id.toInt(),
                authorName = "caffeine_lover",
                commentText = "Facts! Straight espresso is the ultimate purist fuel. Happy creating! 💯",
                timestamp = System.currentTimeMillis() - 3600000 * 40
            )
        )

        // 3. Inbox / General fan messages (Inbox tab) - Unreplied items
        repository.insertComment(
            FanComment(
                postId = 0, // 0 means Inbox DM
                authorName = "brand_collab_hub",
                commentText = "Hey there! Love your distinct aesthetic and creative focus. We have an upcoming design campaign and would love to collaborate on a sponsored post. Do you have a media kit we can check out?",
                timestamp = System.currentTimeMillis() - 2400000
            )
        )

        repository.insertComment(
            FanComment(
                postId = 0,
                authorName = "aspiring_creator_x",
                commentText = "Hello! I am just starting out as a content creator and I struggle so much with caption ideas. How do you consistently write captions that feel so human and interactive?",
                timestamp = System.currentTimeMillis() - 4800000
            )
        )

        repository.insertComment(
            FanComment(
                postId = 0,
                authorName = "graphic_gurus",
                commentText = "Your gradient choices are legendary! Do you design your assets directly on mobile, or do you export from vector studios?",
                timestamp = System.currentTimeMillis() - 7200000,
                isReplied = true,
                replyText = "Thank you! I design my mockup blocks using dynamic Jetpack Compose Canvas and custom gradients, which allows fluid, fully native UI rendering. I do most planning right inside the Creative Creator!"
            )
        )

        // 4. Initial Saved Drafts
        repository.insertPost(
            Post(
                caption = "Drafting standard UI frameworks. Simple layouts + elegant dark tone + custom Compose components.",
                imageUrl = "gradient_4",
                filterName = "Noir",
                likes = 0,
                views = 0,
                commentsCount = 0,
                category = "Tech",
                isDraft = true,
                aiCopilotNotes = "💡 Tip: Expand on this draft! Incorporate a 3-step practical checklist on building layout hierarchies."
            )
        )

        repository.insertPost(
            Post(
                caption = "The ultimate 5-minute creator setup: portable LED light, crisp dynamic microphone, and an elegant notebook. Minimalism at its peak. What's in your creative bag?",
                imageUrl = "gradient_5",
                filterName = "Normal",
                likes = 0,
                views = 0,
                commentsCount = 0,
                category = "Lifestyle",
                isDraft = true
            )
        )
    }
}

// --- Auxiliary Models for Custom Profile, Notifications, and Direct Messages ---
data class NotificationItem(
    val id: Int,
    val type: String, // "LIKE", "FOLLOW", "COMMENT"
    val senderName: String,
    val senderAvatarLetter: String,
    val detailText: String,
    val timestamp: Long,
    val isInteracted: Boolean = false
)

data class ChatMessage(
    val id: Int,
    val senderName: String,
    val text: String,
    val timestamp: Long,
    val isFromMe: Boolean
)

