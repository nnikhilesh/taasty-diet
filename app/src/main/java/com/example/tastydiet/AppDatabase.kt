package com.example.tastydiet

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration
import com.example.tastydiet.data.RecipeDao
import com.example.tastydiet.data.InventoryDao
import com.example.tastydiet.data.MealPlanDao
import com.example.tastydiet.data.ShoppingListDao
import com.example.tastydiet.data.FamilyMemberDao
import com.example.tastydiet.data.WeeklyDietPreferenceDao
import com.example.tastydiet.data.FoodLogDao
import com.example.tastydiet.data.FoodLogEntryDao
import com.example.tastydiet.data.MealLogDao
import com.example.tastydiet.data.PortionResultDao
import com.example.tastydiet.data.GuestInfoDao
import com.example.tastydiet.data.IngredientDao
import com.example.tastydiet.data.MealDao
import com.example.tastydiet.data.RecipeStepDao
import com.example.tastydiet.data.ProfileDao
import com.example.tastydiet.data.AnalyticsDao
import com.example.tastydiet.data.NotificationDao
import com.example.tastydiet.data.NutritionalInfoDao
import com.example.tastydiet.data.RecipeIngredientDao
import com.example.tastydiet.data.SmartMealPlanDao
import com.example.tastydiet.data.GroceryDao
import com.example.tastydiet.data.converters.DateConverter
import com.example.tastydiet.data.DatabaseInitializer
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.WeeklyDietPreference
import com.example.tastydiet.data.models.ShoppingListItem
import com.example.tastydiet.data.models.FamilyMember
import com.example.tastydiet.data.models.MealPlan
import com.example.tastydiet.data.models.Meal
import com.example.tastydiet.data.models.Ingredient
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.FoodLogEntry
import com.example.tastydiet.data.models.MealLog
import com.example.tastydiet.data.models.PortionResult
import com.example.tastydiet.data.models.GuestInfo
import com.example.tastydiet.data.models.RecipeStep
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.AnalyticsData
import com.example.tastydiet.data.models.NotificationSettings
import com.example.tastydiet.data.models.NotificationLog
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.models.MacroPrefConverter
import com.example.tastydiet.data.models.RecipeIngredient
import com.example.tastydiet.data.models.SmartMealPlan
import com.example.tastydiet.data.models.GroceryItem

@Database(
    entities = [
        Recipe::class,
        InventoryItem::class,
        WeeklyDietPreference::class,
        ShoppingListItem::class,
        FamilyMember::class,
        MealPlan::class,
        Meal::class,
        Ingredient::class,
        FoodLog::class,
        FoodLogEntry::class,
        MealLog::class,
        PortionResult::class,
        GuestInfo::class,
        RecipeStep::class,
        Profile::class,
        AnalyticsData::class,
        NotificationSettings::class,
        NotificationLog::class,
        NutritionalInfo::class,
        RecipeIngredient::class,
        SmartMealPlan::class,
        GroceryItem::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(MacroPrefConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun weeklyDietPreferenceDao(): WeeklyDietPreferenceDao
    abstract fun foodLogDao(): FoodLogDao
    abstract fun foodLogEntryDao(): FoodLogEntryDao
    abstract fun mealLogDao(): MealLogDao
    abstract fun portionResultDao(): PortionResultDao
    abstract fun guestInfoDao(): GuestInfoDao
    abstract fun ingredientDao(): IngredientDao
    abstract fun mealDao(): MealDao
    abstract fun recipeStepDao(): RecipeStepDao
    abstract fun profileDao(): ProfileDao
    abstract fun analyticsDao(): AnalyticsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun nutritionalInfoDao(): NutritionalInfoDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun smartMealPlanDao(): SmartMealPlanDao
    abstract fun groceryDao(): GroceryDao

    companion object {
        // Migration from version 1 to 2 - Create grocery_items table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `grocery_items` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`name` TEXT NOT NULL, " +
                    "`category` TEXT NOT NULL, " +
                    "`unit` TEXT NOT NULL, " +
                    "`quantity` REAL NOT NULL DEFAULT 0.0, " +
                    "`isAvailable` INTEGER NOT NULL DEFAULT 0" +
                    ")"
                )
            }
        }
        
        // Migration from version 2 to 3 - Add ingredients column to recipes table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `recipes` ADD COLUMN `ingredients` TEXT NOT NULL DEFAULT ''"
                )
            }
        }
        
        // Migration from version 3 to 4 - Rename InventoryItem table to inventory_items
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `InventoryItem` RENAME TO `inventory_items`"
                )
            }
        }
        
        // Migration from version 4 to 5 - Add meal-specific macro distribution fields to Profile
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new meal-specific macro distribution fields to profiles table
                database.execSQL("ALTER TABLE profiles ADD COLUMN breakfastCaloriesPercent REAL DEFAULT 25.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN breakfastProteinPercent REAL DEFAULT 25.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN breakfastCarbsPercent REAL DEFAULT 30.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN breakfastFatPercent REAL DEFAULT 25.0")
                
                database.execSQL("ALTER TABLE profiles ADD COLUMN lunchCaloriesPercent REAL DEFAULT 35.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN lunchProteinPercent REAL DEFAULT 35.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN lunchCarbsPercent REAL DEFAULT 35.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN lunchFatPercent REAL DEFAULT 35.0")
                
                database.execSQL("ALTER TABLE profiles ADD COLUMN snackCaloriesPercent REAL DEFAULT 15.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN snackProteinPercent REAL DEFAULT 15.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN snackCarbsPercent REAL DEFAULT 15.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN snackFatPercent REAL DEFAULT 15.0")
                
                database.execSQL("ALTER TABLE profiles ADD COLUMN dinnerCaloriesPercent REAL DEFAULT 25.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN dinnerProteinPercent REAL DEFAULT 25.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN dinnerCarbsPercent REAL DEFAULT 20.0")
                database.execSQL("ALTER TABLE profiles ADD COLUMN dinnerFatPercent REAL DEFAULT 25.0")
            }
        }
        
        @Volatile private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tasty_diet_database"
            )
            .fallbackToDestructiveMigration() // This will recreate the database if migration fails
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .addCallback(DatabaseInitializer.getDatabaseCallback())
            .build().also { INSTANCE = it }
        }
        
        fun clearDatabase(context: Context) {
            INSTANCE?.close()
            INSTANCE = null
            context.deleteDatabase("tasty_diet_database")
        }
        
        fun resetDatabase(context: Context) {
            clearDatabase(context)
            getInstance(context) // This will recreate the database with correct schema
        }
    }
} 