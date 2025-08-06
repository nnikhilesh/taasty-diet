package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.RecipeStep

@Dao
interface RecipeStepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipeStep: RecipeStep): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipeSteps: List<RecipeStep>): List<Long>

    @Update
    suspend fun update(recipeStep: RecipeStep)

    @Delete
    suspend fun delete(recipeStep: RecipeStep)

    @Query("SELECT * FROM recipe_steps WHERE id = :id")
    suspend fun getById(id: Int): RecipeStep?

    @Query("SELECT * FROM recipe_steps WHERE recipe_id = :recipeId ORDER BY step_number")
    suspend fun getByRecipeId(recipeId: Int): List<RecipeStep>

    @Query("SELECT COUNT(*) FROM recipe_steps WHERE recipe_id = :recipeId")
    suspend fun getCountByRecipeId(recipeId: Int): Int

    @Query("DELETE FROM recipe_steps WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Int)
}
