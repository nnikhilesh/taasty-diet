package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM Recipe")
    fun getAll(): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE id = :id")
    suspend fun getById(id: Int): Recipe?

    @Query("SELECT * FROM Recipe WHERE category = :category")
    fun getByCategory(category: String): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE cuisine = :cuisine")
    fun getByCuisine(cuisine: String): Flow<List<Recipe>>

    @Query("SELECT * FROM Recipe WHERE name LIKE '%' || :query || '%'")
    fun searchByNameOrIngredient(query: String): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recipes: List<Recipe>)

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("UPDATE Recipe SET liked = :liked WHERE id = :id")
    suspend fun updateLiked(id: Int, liked: Boolean)
    
    fun getAllRecipes(): Flow<List<Recipe>> = getAll()
    
    suspend fun insertAllRecipes(recipes: List<Recipe>) {
        insertAll(recipes)
    }

    // Smart meal planning queries
    @Query("SELECT * FROM Recipe WHERE category = :mealType ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomRecipesForMealType(mealType: String, limit: Int): List<Recipe>

    @Query("SELECT * FROM Recipe WHERE category = :mealType AND caloriesPer100g BETWEEN :minCalories AND :maxCalories ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRecipesForMealTypeWithCalorieRange(mealType: String, minCalories: Int, maxCalories: Int, limit: Int): List<Recipe>

    @Query("SELECT * FROM Recipe WHERE category = :mealType AND proteinPer100g >= :minProtein ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRecipesForMealTypeWithMinProtein(mealType: String, minProtein: Float, limit: Int): List<Recipe>

    @Query("SELECT * FROM Recipe WHERE category = :mealType AND carbsPer100g <= :maxCarbs ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRecipesForMealTypeWithMaxCarbs(mealType: String, maxCarbs: Float, limit: Int): List<Recipe>

    @Query("SELECT * FROM Recipe WHERE category = :mealType AND fatPer100g <= :maxFat ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRecipesForMealTypeWithMaxFat(mealType: String, maxFat: Float, limit: Int): List<Recipe>
} 