package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val caption: String,
    val imageUrl: String? = null,
    val filterName: String = "Normal",
    val likes: Int = 0,
    val views: Int = 0,
    val commentsCount: Int = 0,
    val category: String = "General",
    val timestamp: Long = System.currentTimeMillis(),
    val isDraft: Boolean = false,
    val aiCopilotNotes: String? = null
)
