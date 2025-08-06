package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.Profile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<Profile>>
    
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): Profile?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: Profile): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProfiles(profiles: List<Profile>)
    
    @Update
    suspend fun updateProfile(profile: Profile)
    
    @Delete
    suspend fun deleteProfile(profile: Profile)
    
    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)
    
    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
} 