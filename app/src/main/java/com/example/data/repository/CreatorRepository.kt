package com.example.data.repository

import com.example.data.local.CreatorDao
import com.example.data.model.Post
import com.example.data.model.FanComment
import com.example.data.remote.GeminiWorker
import kotlinx.coroutines.flow.Flow

class CreatorRepository(private val dao: CreatorDao) {

    // --- Exposed Streams for ViewModel ---
    val publishedPosts: Flow<List<Post>> = dao.getAllPublishedPosts()
    val draftPosts: Flow<List<Post>> = dao.getAllDraftPosts()
    val inboxComments: Flow<List<FanComment>> = dao.getAllInboxComments()

    // --- Post DB Operations ---
    fun getCommentsForPost(postId: Int): Flow<List<FanComment>> = dao.getCommentsForPost(postId)

    suspend fun getPostById(id: Int): Post? = dao.getPostById(id)

    suspend fun insertPost(post: Post): Long = dao.insertPost(post)

    suspend fun updatePost(post: Post) = dao.updatePost(post)

    suspend fun deletePost(post: Post) = dao.deletePost(post)

    // --- Comment DB Operations ---
    suspend fun insertComment(comment: FanComment): Long = dao.insertComment(comment)

    suspend fun updateComment(comment: FanComment) = dao.updateComment(comment)

    suspend fun deleteComment(comment: FanComment) = dao.deleteComment(comment)

    // --- Gemini AI Operations ---
    suspend fun generateCaption(topic: String, styleTone: String, keywords: String): String {
        return GeminiWorker.generateCaption(topic, styleTone, keywords)
    }

    suspend fun generateSmartReply(fanComment: String, postCaption: String): String {
        return GeminiWorker.generateSmartReply(fanComment, postCaption)
    }

    suspend fun generatePostFeedback(caption: String, category: String): String {
        return GeminiWorker.generatePostFeedback(caption, category)
    }
}
