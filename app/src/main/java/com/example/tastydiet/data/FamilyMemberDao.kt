package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.FamilyMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM FamilyMember")
    suspend fun getAll(): List<FamilyMember>

    @Query("SELECT * FROM FamilyMember WHERE id = :id")
    suspend fun getById(id: Int): FamilyMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(member: FamilyMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(members: List<FamilyMember>)

    @Update
    suspend fun update(member: FamilyMember)

    @Delete
    suspend fun delete(member: FamilyMember)
    
    fun getAllFamilyMembers(): Flow<List<FamilyMember>> {
        return flow { emit(getAll()) }
    }
    
    suspend fun insertAllFamilyMembers(members: List<FamilyMember>) {
        insertAll(members)
    }
} 