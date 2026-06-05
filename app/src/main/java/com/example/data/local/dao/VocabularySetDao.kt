package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.VocabularySetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularySetDao {
    // Get all sets visible to user (System sets + User's own sets)
    @Query("SELECT * FROM vocabulary_sets WHERE isSystem = 1 OR userId = :userId ORDER BY isSystem DESC, createdAt DESC")
    fun getAllAvailableSetsFlow(userId: Int): Flow<List<VocabularySetEntity>>

    @Query("SELECT * FROM vocabulary_sets WHERE id = :setId LIMIT 1")
    suspend fun getSetById(setId: Int): VocabularySetEntity?

    @Query("SELECT * FROM vocabulary_sets WHERE (isSystem = 1 OR userId = :userId) AND level = :level ORDER BY createdAt DESC")
    fun getSetsByLevelFlow(userId: Int, level: String): Flow<List<VocabularySetEntity>>

    @Query("""
        SELECT * FROM vocabulary_sets 
        WHERE (isSystem = 1 OR userId = :userId) 
        AND (name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%')
    """)
    fun searchSetsFlow(userId: Int, query: String): Flow<List<VocabularySetEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSet(set: VocabularySetEntity): Long

    @Upsert
    suspend fun upsertSet(set: VocabularySetEntity)

    @Update
    suspend fun updateSet(set: VocabularySetEntity)

    @Delete
    suspend fun deleteSet(set: VocabularySetEntity)
}
