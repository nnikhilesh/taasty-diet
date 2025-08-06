package com.example.tastydiet.data

import android.content.Context
import com.example.tastydiet.data.models.NutritionalInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object NutritionalDatabase {
    
    fun getDefaultNutritionalData(): List<NutritionalInfo> {
        return listOf(
            // Grains & Cereals
            NutritionalInfo("Rice (White)", 130f, 2.4f, 28f, 0.3f, 0.4f, "Grains"),
            NutritionalInfo("Rice (Brown)", 111f, 2.6f, 23f, 0.9f, 1.8f, "Grains"),
            NutritionalInfo("Wheat Flour", 364f, 10f, 76f, 1f, 2.7f, "Grains"),
            NutritionalInfo("Chapati", 264f, 7.5f, 55f, 0.5f, 2.5f, "Grains"),
            NutritionalInfo("Roti", 264f, 7.5f, 55f, 0.5f, 2.5f, "Grains"),
            NutritionalInfo("Bread (White)", 265f, 9f, 49f, 3.2f, 2.7f, "Grains"),
            NutritionalInfo("Bread (Brown)", 247f, 13f, 41f, 4.2f, 7f, "Grains"),
            NutritionalInfo("Oats", 389f, 16.9f, 66f, 6.9f, 10.6f, "Grains"),
            NutritionalInfo("Quinoa", 120f, 4.4f, 22f, 1.9f, 2.8f, "Grains"),
            NutritionalInfo("Millet", 378f, 11f, 73f, 4.2f, 8.5f, "Grains"),
            
            // Pulses & Legumes
            NutritionalInfo("Lentils (Red)", 116f, 9f, 20f, 0.4f, 7.9f, "Proteins"),
            NutritionalInfo("Lentils (Yellow)", 116f, 9f, 20f, 0.4f, 7.9f, "Proteins"),
            NutritionalInfo("Chickpeas", 164f, 8.9f, 27f, 2.6f, 7.6f, "Proteins"),
            NutritionalInfo("Kidney Beans", 127f, 8.7f, 23f, 0.5f, 6.4f, "Proteins"),
            NutritionalInfo("Black Beans", 132f, 8.9f, 23f, 0.5f, 8.7f, "Proteins"),
            NutritionalInfo("Green Peas", 84f, 5.4f, 14f, 0.4f, 5.7f, "Proteins"),
            NutritionalInfo("Soybeans", 173f, 16.6f, 9.9f, 9f, 6f, "Proteins"),
            NutritionalInfo("Mung Beans", 105f, 7f, 19f, 0.4f, 7.6f, "Proteins"),
            NutritionalInfo("Urad Dal", 341f, 25f, 59f, 1.6f, 18.3f, "Proteins"),
            NutritionalInfo("Toor Dal", 343f, 22f, 61f, 1.6f, 15f, "Proteins"),
            
            // Dairy & Eggs
            NutritionalInfo("Milk (Whole)", 61f, 3.2f, 4.8f, 3.3f, 0f, "Dairy"),
            NutritionalInfo("Milk (Skim)", 42f, 3.4f, 5f, 0.1f, 0f, "Dairy"),
            NutritionalInfo("Yogurt (Plain)", 59f, 10f, 3.6f, 0.4f, 0f, "Dairy"),
            NutritionalInfo("Curd", 59f, 10f, 3.6f, 0.4f, 0f, "Dairy"),
            NutritionalInfo("Paneer", 265f, 18f, 1.2f, 20f, 0f, "Dairy"),
            NutritionalInfo("Cheese (Cheddar)", 403f, 25f, 1.3f, 33f, 0f, "Dairy"),
            NutritionalInfo("Butter", 717f, 0.9f, 0.1f, 81f, 0f, "Dairy"),
            NutritionalInfo("Ghee", 900f, 0f, 0f, 100f, 0f, "Dairy"),
            NutritionalInfo("Egg (Whole)", 155f, 13f, 1.1f, 11f, 0f, "Dairy"),
            NutritionalInfo("Egg White", 52f, 11f, 0.7f, 0.2f, 0f, "Dairy"),
            
            // Vegetables
            NutritionalInfo("Potato", 77f, 2f, 17f, 0.1f, 2.2f, "Vegetables"),
            NutritionalInfo("Tomato", 18f, 0.9f, 3.9f, 0.2f, 1.2f, "Vegetables"),
            NutritionalInfo("Onion", 40f, 1.1f, 9.3f, 0.1f, 1.7f, "Vegetables"),
            NutritionalInfo("Carrot", 41f, 0.9f, 10f, 0.2f, 2.8f, "Vegetables"),
            NutritionalInfo("Cucumber", 16f, 0.7f, 3.6f, 0.1f, 0.5f, "Vegetables"),
            NutritionalInfo("Spinach", 23f, 2.9f, 3.6f, 0.4f, 2.2f, "Vegetables"),
            NutritionalInfo("Cabbage", 25f, 1.3f, 5.8f, 0.1f, 2.5f, "Vegetables"),
            NutritionalInfo("Cauliflower", 25f, 1.9f, 5f, 0.3f, 2f, "Vegetables"),
            NutritionalInfo("Broccoli", 34f, 2.8f, 7f, 0.4f, 2.6f, "Vegetables"),
            NutritionalInfo("Bell Pepper", 20f, 0.9f, 4.6f, 0.2f, 1.7f, "Vegetables"),
            NutritionalInfo("Mushroom", 22f, 3.1f, 3.3f, 0.3f, 1f, "Vegetables"),
            NutritionalInfo("Sweet Potato", 86f, 1.6f, 20f, 0.1f, 3f, "Vegetables"),
            NutritionalInfo("Pumpkin", 26f, 1f, 6.5f, 0.1f, 0.5f, "Vegetables"),
            NutritionalInfo("Brinjal", 25f, 1f, 6f, 0.2f, 3f, "Vegetables"),
            NutritionalInfo("Okra", 33f, 2f, 7f, 0.2f, 3.2f, "Vegetables"),
            NutritionalInfo("Green Beans", 31f, 1.8f, 7f, 0.2f, 2.7f, "Vegetables"),
            NutritionalInfo("Peas", 84f, 5.4f, 14f, 0.4f, 5.7f, "Vegetables"),
            NutritionalInfo("Corn", 86f, 3.2f, 19f, 1.2f, 2.7f, "Vegetables"),
            NutritionalInfo("Lettuce", 15f, 1.4f, 2.9f, 0.2f, 1.3f, "Vegetables"),
            NutritionalInfo("Kale", 49f, 4.3f, 8.8f, 0.9f, 3.6f, "Vegetables"),
            NutritionalInfo("Arugula", 25f, 2.6f, 3.7f, 0.7f, 1.6f, "Vegetables"),
            NutritionalInfo("Radish", 16f, 0.7f, 3.4f, 0.1f, 1.6f, "Vegetables"),
            NutritionalInfo("Beetroot", 43f, 1.6f, 10f, 0.2f, 2.8f, "Vegetables"),
            NutritionalInfo("Turnip", 28f, 0.9f, 6.4f, 0.1f, 1.8f, "Vegetables"),
            NutritionalInfo("Parsnip", 75f, 1.2f, 18f, 0.3f, 4.9f, "Vegetables"),
            NutritionalInfo("Celery", 16f, 0.7f, 3f, 0.2f, 1.6f, "Vegetables"),
            NutritionalInfo("Asparagus", 20f, 2.2f, 3.9f, 0.1f, 2.1f, "Vegetables"),
            NutritionalInfo("Artichoke", 47f, 3.3f, 11f, 0.2f, 5.4f, "Vegetables"),
            NutritionalInfo("Zucchini", 17f, 1.2f, 3.1f, 0.3f, 1f, "Vegetables"),
            NutritionalInfo("Squash", 31f, 1.2f, 7f, 0.3f, 1.2f, "Vegetables"),
            NutritionalInfo("Eggplant", 25f, 1f, 6f, 0.2f, 3f, "Vegetables"),
            NutritionalInfo("Leek", 61f, 1.5f, 14f, 0.3f, 1.8f, "Vegetables"),
            NutritionalInfo("Garlic", 149f, 6.4f, 33f, 0.5f, 2.1f, "Vegetables"),
            NutritionalInfo("Ginger", 80f, 1.8f, 18f, 0.8f, 2f, "Vegetables"),
            NutritionalInfo("Turmeric", 354f, 8f, 65f, 10f, 21f, "Vegetables"),
            NutritionalInfo("Chili Pepper", 40f, 1.9f, 9f, 0.4f, 1.5f, "Vegetables"),
            NutritionalInfo("Jalapeno", 29f, 0.9f, 6.5f, 0.4f, 2.8f, "Vegetables"),
            NutritionalInfo("Habanero", 33f, 1.3f, 7.3f, 0.2f, 3.9f, "Vegetables"),
            NutritionalInfo("Cayenne", 318f, 12f, 56f, 17f, 27f, "Vegetables"),
            NutritionalInfo("Paprika", 282f, 14f, 54f, 13f, 35f, "Vegetables"),
            NutritionalInfo("Cumin", 375f, 18f, 44f, 22f, 11f, "Vegetables"),
            NutritionalInfo("Coriander", 23f, 2.1f, 3.7f, 0.5f, 2.8f, "Vegetables"),
            NutritionalInfo("Parsley", 36f, 3f, 6.3f, 0.8f, 3.3f, "Vegetables"),
            NutritionalInfo("Basil", 22f, 3.2f, 2.6f, 0.6f, 1.6f, "Vegetables"),
            NutritionalInfo("Oregano", 265f, 9f, 69f, 4f, 43f, "Vegetables"),
            NutritionalInfo("Thyme", 101f, 5.6f, 24f, 1.7f, 14f, "Vegetables"),
            NutritionalInfo("Rosemary", 131f, 3.3f, 21f, 5.9f, 14f, "Vegetables"),
            NutritionalInfo("Sage", 315f, 11f, 61f, 13f, 40f, "Vegetables"),
            NutritionalInfo("Mint", 44f, 3.8f, 8.4f, 0.7f, 8f, "Vegetables"),
            NutritionalInfo("Dill", 43f, 3.5f, 7f, 1.1f, 2.1f, "Vegetables"),
            NutritionalInfo("Bay Leaf", 313f, 8f, 75f, 8f, 26f, "Vegetables"),
            NutritionalInfo("Cinnamon", 247f, 4f, 81f, 1.2f, 54f, "Vegetables"),
            NutritionalInfo("Nutmeg", 525f, 6f, 49f, 36f, 21f, "Vegetables"),
            NutritionalInfo("Cloves", 274f, 6f, 66f, 13f, 34f, "Vegetables"),
            NutritionalInfo("Cardamom", 311f, 11f, 68f, 7f, 28f, "Vegetables"),
            NutritionalInfo("Star Anise", 337f, 18f, 50f, 16f, 15f, "Vegetables"),
            NutritionalInfo("Fennel", 31f, 1.2f, 7f, 0.2f, 3.1f, "Vegetables"),
            NutritionalInfo("Anise", 337f, 18f, 50f, 16f, 15f, "Vegetables"),
            NutritionalInfo("Caraway", 333f, 20f, 50f, 15f, 38f, "Vegetables"),
            NutritionalInfo("Mustard", 508f, 26f, 28f, 36f, 12f, "Vegetables"),
            NutritionalInfo("Horseradish", 48f, 1.2f, 11f, 0.7f, 3.3f, "Vegetables"),
            NutritionalInfo("Wasabi", 109f, 5f, 24f, 0.6f, 8f, "Vegetables"),
            
            // Fruits
            NutritionalInfo("Apple", 52f, 0.3f, 14f, 0.2f, 2.4f, "Fruits"),
            NutritionalInfo("Banana", 89f, 1.1f, 23f, 0.3f, 2.6f, "Fruits"),
            NutritionalInfo("Orange", 47f, 0.9f, 12f, 0.1f, 2.4f, "Fruits"),
            NutritionalInfo("Mango", 60f, 0.8f, 15f, 0.4f, 1.6f, "Fruits"),
            NutritionalInfo("Pineapple", 50f, 0.5f, 13f, 0.1f, 1.4f, "Fruits"),
            NutritionalInfo("Strawberry", 32f, 0.7f, 8f, 0.3f, 2f, "Fruits"),
            NutritionalInfo("Grapes", 62f, 0.6f, 16f, 0.2f, 0.9f, "Fruits"),
            NutritionalInfo("Watermelon", 30f, 0.6f, 8f, 0.2f, 0.4f, "Fruits"),
            NutritionalInfo("Papaya", 43f, 0.5f, 11f, 0.3f, 1.7f, "Fruits"),
            NutritionalInfo("Guava", 68f, 2.6f, 14f, 0.9f, 5.4f, "Fruits"),
            NutritionalInfo("Pomegranate", 83f, 1.7f, 19f, 1.2f, 4f, "Fruits"),
            NutritionalInfo("Coconut", 354f, 3.3f, 15f, 33f, 9f, "Fruits"),
            NutritionalInfo("Kiwi", 61f, 1.1f, 15f, 0.5f, 3f, "Fruits"),
            NutritionalInfo("Peach", 39f, 0.9f, 10f, 0.3f, 1.5f, "Fruits"),
            NutritionalInfo("Pear", 57f, 0.4f, 15f, 0.1f, 3.1f, "Fruits"),
            NutritionalInfo("Plum", 46f, 0.7f, 11f, 0.3f, 1.4f, "Fruits"),
            NutritionalInfo("Cherry", 50f, 1f, 12f, 0.3f, 1.6f, "Fruits"),
            NutritionalInfo("Blueberry", 57f, 0.7f, 14f, 0.3f, 2.4f, "Fruits"),
            NutritionalInfo("Raspberry", 52f, 1.2f, 12f, 0.7f, 6.5f, "Fruits"),
            NutritionalInfo("Blackberry", 43f, 1.4f, 10f, 0.5f, 5.3f, "Fruits"),
            NutritionalInfo("Cranberry", 46f, 0.4f, 12f, 0.1f, 3.6f, "Fruits"),
            NutritionalInfo("Lemon", 29f, 1.1f, 9f, 0.3f, 2.8f, "Fruits"),
            NutritionalInfo("Lime", 30f, 0.7f, 10f, 0.2f, 2.8f, "Fruits"),
            NutritionalInfo("Tangerine", 53f, 0.8f, 13f, 0.3f, 1.8f, "Fruits"),
            NutritionalInfo("Clementine", 47f, 0.9f, 12f, 0.2f, 2.2f, "Fruits"),
            NutritionalInfo("Fig", 74f, 0.8f, 19f, 0.3f, 2.9f, "Fruits"),
            NutritionalInfo("Date", 282f, 2.5f, 75f, 0.4f, 8f, "Fruits"),
            NutritionalInfo("Raisin", 299f, 3.1f, 79f, 0.5f, 3.7f, "Fruits"),
            NutritionalInfo("Prune", 240f, 2.2f, 64f, 0.4f, 7.1f, "Fruits"),
            NutritionalInfo("Apricot", 48f, 1.4f, 11f, 0.4f, 2f, "Fruits"),
            NutritionalInfo("Nectarine", 44f, 1.1f, 11f, 0.3f, 1.7f, "Fruits"),
            
            // Nuts & Seeds
            NutritionalInfo("Almonds", 579f, 21f, 22f, 50f, 12.5f, "Nuts"),
            NutritionalInfo("Walnuts", 654f, 15f, 14f, 65f, 6.7f, "Nuts"),
            NutritionalInfo("Cashews", 553f, 18f, 30f, 44f, 3.3f, "Nuts"),
            NutritionalInfo("Peanuts", 567f, 26f, 16f, 49f, 8.5f, "Nuts"),
            NutritionalInfo("Pistachios", 560f, 20f, 28f, 45f, 10.6f, "Nuts"),
            NutritionalInfo("Sunflower Seeds", 584f, 21f, 20f, 51f, 8.6f, "Nuts"),
            NutritionalInfo("Pumpkin Seeds", 559f, 19f, 54f, 19f, 18.4f, "Nuts"),
            NutritionalInfo("Chia Seeds", 486f, 17f, 42f, 31f, 34.4f, "Nuts"),
            NutritionalInfo("Flax Seeds", 534f, 18f, 29f, 42f, 27.3f, "Nuts"),
            
            // Oils & Fats
            NutritionalInfo("Olive Oil", 884f, 0f, 0f, 100f, 0f, "Oils"),
            NutritionalInfo("Coconut Oil", 862f, 0f, 0f, 100f, 0f, "Oils"),
            NutritionalInfo("Mustard Oil", 884f, 0f, 0f, 100f, 0f, "Oils"),
            NutritionalInfo("Sesame Oil", 884f, 0f, 0f, 100f, 0f, "Oils"),
            NutritionalInfo("Sunflower Oil", 884f, 0f, 0f, 100f, 0f, "Oils"),
            
            // Spices & Condiments
            NutritionalInfo("Turmeric", 354f, 8f, 65f, 10f, 21f, "Spices"),
            NutritionalInfo("Ginger", 80f, 1.8f, 18f, 0.8f, 2f, "Spices"),
            NutritionalInfo("Garlic", 149f, 6.4f, 33f, 0.5f, 2.1f, "Spices"),
            NutritionalInfo("Cumin", 375f, 18f, 44f, 22f, 11f, "Spices"),
            NutritionalInfo("Coriander", 298f, 12f, 55f, 18f, 41f, "Spices"),
            NutritionalInfo("Black Pepper", 251f, 10f, 64f, 3.3f, 25f, "Spices"),
            NutritionalInfo("Chili Powder", 282f, 12f, 49f, 12f, 34f, "Spices"),
            NutritionalInfo("Salt", 0f, 0f, 0f, 0f, 0f, "Spices"),
            NutritionalInfo("Sugar", 387f, 0f, 100f, 0f, 0f, "Spices"),
            NutritionalInfo("Honey", 304f, 0.3f, 82f, 0f, 0.2f, "Spices"),
            
            // Meat & Fish
            NutritionalInfo("Chicken Breast", 165f, 31f, 0f, 3.6f, 0f, "Proteins"),
            NutritionalInfo("Chicken Thigh", 209f, 26f, 0f, 12f, 0f, "Proteins"),
            NutritionalInfo("Fish (Salmon)", 208f, 25f, 0f, 12f, 0f, "Proteins"),
            NutritionalInfo("Fish (Tuna)", 144f, 30f, 0f, 1f, 0f, "Proteins"),
            NutritionalInfo("Eggs", 155f, 13f, 1.1f, 11f, 0f, "Proteins"),
            NutritionalInfo("Lamb", 294f, 25f, 0f, 21f, 0f, "Proteins"),
            NutritionalInfo("Beef", 250f, 26f, 0f, 15f, 0f, "Proteins"),
            NutritionalInfo("Pork", 242f, 27f, 0f, 14f, 0f, "Proteins"),
            
            // Processed Foods
            NutritionalInfo("Pasta", 131f, 5f, 25f, 1.1f, 1.8f, "Processed"),
            NutritionalInfo("Noodles", 138f, 4.5f, 25f, 2.1f, 1.2f, "Processed"),
            NutritionalInfo("Biscuits", 450f, 6f, 70f, 15f, 2f, "Processed"),
            NutritionalInfo("Chips", 536f, 7f, 53f, 35f, 4.4f, "Processed"),
            NutritionalInfo("Chocolate", 545f, 4.9f, 61f, 31f, 7f, "Processed"),
            NutritionalInfo("Ice Cream", 207f, 3.5f, 24f, 11f, 0f, "Processed"),
            NutritionalInfo("Cake", 257f, 5.2f, 45f, 5.1f, 1.2f, "Processed"),
            NutritionalInfo("Cookies", 502f, 5.7f, 65f, 24f, 2.2f, "Processed"),
            
            // Beverages
            NutritionalInfo("Coffee", 2f, 0.3f, 0f, 0f, 0f, "Beverages"),
            NutritionalInfo("Tea", 1f, 0f, 0f, 0f, 0f, "Beverages"),
            NutritionalInfo("Orange Juice", 45f, 0.7f, 10f, 0.2f, 0.2f, "Beverages"),
            NutritionalInfo("Apple Juice", 46f, 0.1f, 11f, 0.1f, 0.2f, "Beverages"),
            NutritionalInfo("Milk Shake", 88f, 3.2f, 14f, 2.5f, 0f, "Beverages"),
            NutritionalInfo("Soda", 42f, 0f, 11f, 0f, 0f, "Beverages"),
            
            // Indian Dishes (Approximate)
            NutritionalInfo("Dal", 120f, 6f, 20f, 2f, 5f, "Indian Dishes"),
            NutritionalInfo("Curry", 150f, 8f, 15f, 8f, 3f, "Indian Dishes"),
            NutritionalInfo("Biryani", 280f, 12f, 45f, 8f, 4f, "Indian Dishes"),
            NutritionalInfo("Kebab", 220f, 18f, 8f, 12f, 2f, "Indian Dishes"),
            NutritionalInfo("Samosa", 260f, 6f, 35f, 12f, 3f, "Indian Dishes"),
            NutritionalInfo("Pakora", 200f, 5f, 25f, 10f, 2f, "Indian Dishes"),
            NutritionalInfo("Dosa", 180f, 5f, 30f, 5f, 3f, "Indian Dishes"),
            NutritionalInfo("Idli", 120f, 4f, 22f, 2f, 2f, "Indian Dishes"),
            NutritionalInfo("Vada", 250f, 6f, 35f, 10f, 3f, "Indian Dishes"),
            NutritionalInfo("Upma", 160f, 4f, 28f, 4f, 2f, "Indian Dishes"),
            NutritionalInfo("Poha", 140f, 3f, 26f, 3f, 2f, "Indian Dishes"),
            NutritionalInfo("Khichdi", 180f, 8f, 30f, 4f, 4f, "Indian Dishes"),
            NutritionalInfo("Pulao", 220f, 6f, 40f, 6f, 3f, "Indian Dishes"),
            NutritionalInfo("Naan", 280f, 8f, 50f, 6f, 2f, "Indian Dishes"),
            NutritionalInfo("Paratha", 300f, 8f, 45f, 10f, 3f, "Indian Dishes"),
            NutritionalInfo("Poori", 320f, 6f, 50f, 12f, 2f, "Indian Dishes"),
            NutritionalInfo("Bhatura", 340f, 7f, 55f, 10f, 2f, "Indian Dishes"),
            NutritionalInfo("Kulfi", 200f, 4f, 30f, 8f, 0f, "Indian Dishes"),
            NutritionalInfo("Gulab Jamun", 320f, 4f, 50f, 12f, 1f, "Indian Dishes"),
            NutritionalInfo("Rasgulla", 280f, 3f, 55f, 8f, 1f, "Indian Dishes"),
            NutritionalInfo("Jalebi", 300f, 2f, 60f, 8f, 1f, "Indian Dishes"),
            NutritionalInfo("Lassi", 120f, 3f, 20f, 4f, 0f, "Indian Dishes"),
            NutritionalInfo("Chai", 40f, 1f, 8f, 1f, 0f, "Indian Dishes")
        )
    }
    
    suspend fun loadComprehensiveDatabase(context: Context): List<NutritionalInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("comprehensive_food_database.json").bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(jsonString)
                val foodItemsArray = jsonObject.getJSONArray("foodItems")
                
                val comprehensiveFoodItems = mutableListOf<NutritionalInfo>()
                
                for (i in 0 until foodItemsArray.length()) {
                    val foodItem = foodItemsArray.getJSONObject(i)
                    
                    val nutritionalInfo = NutritionalInfo(
                        name = foodItem.getString("name"),
                        caloriesPer100g = foodItem.getDouble("caloriesPerUnit").toFloat(),
                        proteinPer100g = foodItem.getDouble("proteinPerUnit").toFloat(),
                        carbsPer100g = foodItem.getDouble("carbsPerUnit").toFloat(),
                        fatPer100g = foodItem.getDouble("fatPerUnit").toFloat(),
                        fiberPer100g = foodItem.getDouble("fiberPerUnit").toFloat(),
                        category = foodItem.getString("category")
                    )
                    
                    comprehensiveFoodItems.add(nutritionalInfo)
                }
                
                comprehensiveFoodItems
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to default data if loading fails
                getDefaultNutritionalData()
            }
        }
    }
} 