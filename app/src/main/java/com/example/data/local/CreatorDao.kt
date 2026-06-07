package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Post
import com.example.data.model.FanComment
import kotlinx.coroutines.flow.Flow

@Dao
interface CreatorDao {

    // --- Post SQL Queries ---

    @Query("SELECT * FROM posts WHERE isDraft = 0 ORDER BY timestamp DESC")
    fun getAllPublishedPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE isDraft = 1 ORDER BY timestamp DESC")
    fun getAllDraftPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getPostById(id: Int): Post?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Update
    suspend fun updatePost(post: Post)

    @Delete
    suspend fun deletePost(post: Post)

    // --- Comment SQL Queries ---

    @Query("SELECT * FROM fan_comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<FanComment>>

    @Query("SELECT * FROM fan_comments ORDER BY timestamp DESC")
    fun getAllInboxComments(): Flow<List<FanComment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: FanComment): Long

    @Update
    suspend fun updateComment(comment: FanComment)

    @Delete
    suspend fun deleteComment(comment: FanComment)
}
