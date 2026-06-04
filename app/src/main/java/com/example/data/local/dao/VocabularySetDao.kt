package com.example.data.local.dao
import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularySetDao {
    // --- Vocabulary Set Queries ---
    @Query("SELECT * FROM vocabulary_sets ORDER BY createdAt DESC")
    fun getAllSetsFlow(): Flow<List<VocabularySetEntity>>

    @Query("SELECT * FROM vocabulary_sets WHERE id = :setId LIMIT 1")
    suspend fun getSetById(setId: Int): VocabularySetEntity?

    @Query("SELECT * FROM vocabulary_sets WHERE name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchSetsFlow(query: String): Flow<List<VocabularySetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: VocabularySetEntity): Long

    @Update
    suspend fun updateSet(set: VocabularySetEntity)

    @Delete
    suspend fun deleteSet(set: VocabularySetEntity)
}