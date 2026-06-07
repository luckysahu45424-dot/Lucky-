package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fan_comments")
data class FanComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int = 0, // Indicates association with a specific post or general inbox if 0
    val authorName: String,
    val authorAvatarUrl: String? = null,
    val commentText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isReplied: Boolean = false,
    val replyText: String? = null,
    val isLiked: Boolean = false
)
