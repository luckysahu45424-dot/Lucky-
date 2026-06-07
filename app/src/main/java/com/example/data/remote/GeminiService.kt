package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

object GeminiWorker {

    /**
     * Checks if a valid non-placeholder API key exists in BuildConfig.
     */
    fun hasValidApiKey(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && key != "GEMINI_API_KEY"
    }

    /**
     * Generates an Instagram post caption based on user's theme selection, focus keywords, and style tone.
     */
    suspend fun generateCaption(topic: String, styleTone: String, keywords: String): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext getMockCaption(topic, styleTone, keywords)
        }

        val prompt = """
            Create a highly engaging, professional Instagram post caption about the following topic: "$topic".
            Keep it in a "$styleTone" aesthetic tone.
            Include these keywords or tags if applicable: "$keywords".
            
            Structure the caption with:
            1. An attention-grabbing hook (within the first 2 lines)
            2. Concise value or story body text spaced elegantly
            3. A clear Call to Action (CTA) encouraging follower interaction (e.g., questions, comments)
            4. 3 to 6 highly relevant, trending hashtags.
            
            Do NOT include meta-text, markdown bold in the core text except for emphasis spacing, and keep it formatted exactly like a clean Instagram post.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No caption could be generated. Let's try listing core hooks instead!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error generating caption: ${e.localizedMessage}. Fallback to local ideas:\n\n${getMockCaption(topic, styleTone, keywords)}"
        }
    }

    /**
     * Generates a conversational reply to a fan's comment, matching the tone of an active influencer.
     */
    suspend fun generateSmartReply(fanComment: String, postCaption: String): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext getMockReply(fanComment, postCaption)
        }

        val prompt = """
            You are a popular, welcoming Instagram content creator.
            A follower left this comment on your recent post:
            Post: "$postCaption"
            Follower's Comment: "$fanComment"
            
            Write a very warm, engaging, and personal reply (1 to 2 lines long) to this follower. Keep it friendly, authentic, and ending with a pleasant, micro-interactive question or emoji that builds community. Do not sound like an generic robot assistant. Speak as a personal influencer.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Thank you so much! Truly appreciate the support. What did you think of the concept?"
        } catch (e: Exception) {
            e.printStackTrace()
            getMockReply(fanComment, postCaption)
        }
    }

    /**
     * Analyzes an Instagram draft caption and suggests professional improvement feedback.
     */
    suspend fun generatePostFeedback(caption: String, category: String): String = withContext(Dispatchers.IO) {
        if (!hasValidApiKey()) {
            return@withContext "💡 Tip: Make your hook more personal! \n🎯 Spacing: Double space before your Call-to-Action to increase visual scannability.\n📈 Trend: Posts in $category are currently getting 24% higher reach when incorporating personal micro-vlogs in stories."
        }

        val prompt = """
            Analyze this Instagram draft caption for a post in the "$category" niche.
            Draft: "$caption"
            
            Provide exactly 2 concise, highly actionable tips to improve engagement, readability, or SEO tags for this post.
            Format each tip on a new line started with a relevant emoji, with no other introductory text.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        try {
            val response = RetrofitClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "💡 Hook: State a shocking stat in the first line!\n🏷️ Tags: Try using niche tags rather than generic broad ones."
        } catch (e: Exception) {
            "💡 Hook: Keep your first sentence under 15 words to prevent clipping as '... see more'.\n💬 Question: End with an absolute question to boost comment response rates!"
        }
    }

    // --- High-Fidelity Local Fallbacks (If API key is absent) ---

    private fun getMockCaption(topic: String, styleTone: String, keywords: String): String {
        val topicClean = if (topic.isBlank()) "Daily vibes" else topic
        val hashKeywords = keywords.split(",")
            .map { it.trim().replace(" ", "").lowercase() }
            .filter { it.isNotBlank() }
            .joinToString(" ") { "#$it" }

        val systemHashtags = when(styleTone.lowercase()) {
            "witty" -> "#creator #creativemind #worklife #dailycomedy"
            "philosophical" -> "#mindset #mindfulness #reflection #growth"
            "short & punchy" -> "#vibe #minimal #postfortheday"
            else -> "#creatorcommunity #trending #studio #vibes"
        }

        val hooks = mapOf(
            "witty" to listOf(
                "You won't believe how long this took to construct, but here we are. 😂",
                "Self-care is looking at a blank screen for 3 hours, then deciding to post anyway.",
                "Adulting is 10% creating content and 90% wondering if the algorithm likes me today."
            ),
            "philosophical" to listOf(
                "In a world of constant motion, sometimes the boldest thing we can do is stay still and observe.",
                "The energy you put into creating is never lost; it simply takes another shape elsewhere.",
                "Growth is built in the silent drafts, not just the published moments. Reflecting on this."
            ),
            "short & punchy" to listOf(
                "Focus on the process.",
                "Behind the lens today. ✨",
                "Simple, yet complete."
            ),
            "hype" to listOf(
                "🚨 EXCLUSIVE ANNOUNCEMENT! We are finally bringing this project live! Let's go! 🚀",
                "GET READY. This is about to completely shift how we approach creating! 🔥",
                "Big energy only today. We're leveling up the design system!"
            )
        )

        val selectedHooks = hooks[styleTone.lowercase()] ?: hooks.values.first()
        val randomIndex = (topicClean.hashCode() % selectedHooks.size).let { if (it < 0) -it else it }
        val hook = selectedHooks[randomIndex]

        return """
            $hook
            
            Spent some quality time diving into "$topicClean". It's been an incredible journey finding new perspectives on this.
            
            Tell me in the comment section: What's your biggest challenge when working on something like this? 👇
            
            $hashKeywords $systemHashtags
        """.trimIndent()
    }

    private fun getMockReply(comment: String, caption: String): String {
        val cleanComment = comment.lowercase()
        return when {
            cleanComment.contains("love") || cleanComment.contains("amazing") || cleanComment.contains("perfect") -> {
                "Thank you so much! Hearing this means the world. Are you working on something similar today? 😊"
            }
            cleanComment.contains("how") || cleanComment.contains("question") || cleanComment.contains("tutorial") -> {
                "Great question! I'm planning to share a full breakdown step-by-step in my next story. Stay tuned! 🚀"
            }
            else -> {
                "Appreciate you stopping by and leaving your thoughts! Always a pleasure to connect. Let's keep creating! ✨"
            }
        }
    }
}
