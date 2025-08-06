package com.example.tastydiet.utils

object RecipeVideoManager {
    
    // Recipe video IDs mapping
    private val recipeVideos = mapOf(
        "vegetable-stir-fry-brown-rice" to "dQw4w9WgXcQ", // Replace with actual video ID
        "grilled-chicken-quinoa-salad" to "dQw4w9WgXcQ", // Replace with actual video ID
        "lentil-soup-whole-grain-bread" to "dQw4w9WgXcQ", // Replace with actual video ID
        "greek-yogurt-berries-nuts" to "dQw4w9WgXcQ", // Replace with actual video ID
        "baked-salmon-roasted-vegetables" to "dQw4w9WgXcQ", // Replace with actual video ID
        
        // Indian recipes
        "dal-rice" to "dQw4w9WgXcQ",
        "chapati-sabzi" to "dQw4w9WgXcQ",
        "idli-sambar" to "dQw4w9WgXcQ",
        "dosa-chutney" to "dQw4w9WgXcQ",
        "biryani" to "dQw4w9WgXcQ",
        "butter-chicken" to "dQw4w9WgXcQ",
        "palak-paneer" to "dQw4w9WgXcQ",
        "rajma-chawal" to "dQw4w9WgXcQ",
        "chole-bhature" to "dQw4w9WgXcQ",
        "aloo-paratha" to "dQw4w9WgXcQ"
    )
    
    /**
     * Get video ID for a recipe
     */
    fun getVideoId(recipeKey: String): String? {
        return recipeVideos[recipeKey]
    }
    
    /**
     * Get YouTube URL for a recipe
     */
    fun getYouTubeUrl(recipeKey: String): String? {
        val videoId = getVideoId(recipeKey)
        return videoId?.let { "https://www.youtube.com/watch?v=$it" }
    }
    
    /**
     * Extract recipe key from YouTube URL format
     */
    fun extractRecipeKeyFromUrl(url: String): String? {
        return if (url.startsWith("youtube://")) {
            url.substringAfter("youtube://")
        } else {
            null
        }
    }
    
    /**
     * Get recipe name from key
     */
    fun getRecipeName(recipeKey: String): String {
        return when (recipeKey) {
            "vegetable-stir-fry-brown-rice" -> "Vegetable Stir-Fry with Brown Rice"
            "grilled-chicken-quinoa-salad" -> "Grilled Chicken with Quinoa Salad"
            "lentil-soup-whole-grain-bread" -> "Lentil Soup with Whole Grain Bread"
            "greek-yogurt-berries-nuts" -> "Greek Yogurt with Berries and Nuts"
            "baked-salmon-roasted-vegetables" -> "Baked Salmon with Roasted Vegetables"
            "dal-rice" -> "Dal Rice"
            "chapati-sabzi" -> "Chapati with Sabzi"
            "idli-sambar" -> "Idli with Sambar"
            "dosa-chutney" -> "Dosa with Chutney"
            "biryani" -> "Biryani"
            "butter-chicken" -> "Butter Chicken"
            "palak-paneer" -> "Palak Paneer"
            "rajma-chawal" -> "Rajma Chawal"
            "chole-bhature" -> "Chole Bhature"
            "aloo-paratha" -> "Aloo Paratha"
            else -> recipeKey.replace("-", " ").capitalize()
        }
    }
    
    /**
     * Get all available recipe keys
     */
    fun getAllRecipeKeys(): List<String> {
        return recipeVideos.keys.toList()
    }
    
    /**
     * Check if recipe has video
     */
    fun hasVideo(recipeKey: String): Boolean {
        return recipeVideos.containsKey(recipeKey)
    }
} 