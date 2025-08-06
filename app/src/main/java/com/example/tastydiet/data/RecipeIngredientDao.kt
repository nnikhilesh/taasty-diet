package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.RecipeIngredient
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: Int): List<RecipeIngredient>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId IN (:recipeIds)")
    suspend fun getIngredientsForRecipes(recipeIds: List<Int>): List<RecipeIngredient>

    @Query("SELECT ingredientName FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredientNamesForRecipe(recipeId: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipeIngredient: RecipeIngredient): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipeIngredients: List<RecipeIngredient>)

    @Update
    suspend fun update(recipeIngredient: RecipeIngredient)

    @Delete
    suspend fun delete(recipeIngredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Int)

    @Query("SELECT DISTINCT ingredientName FROM recipe_ingredients")
    suspend fun getAllIngredientNames(): List<String>
} 