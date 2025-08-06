package com.example.tastydiet.util

import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.data.models.Ingredient

/**
 * Decrement inventory quantities for a recipe's ingredients.
 * For each ingredient in the recipe, subtract required quantity from matching inventory items.
 * If insufficient inventory, skip or warn (here: skip and print warning).
 * Updates the inventoryList in-place.
 *
 * @param recipe The recipe whose ingredients are required
 * @param inventoryList The mutable inventory list to update
 * @param ingredientsForRecipe List of ingredients for this recipe
 */
fun decrementInventoryForRecipe(
    recipe: Recipe,
    inventoryList: MutableList<InventoryItem>,
    ingredientsForRecipe: List<Ingredient>
) {
    for (ingredient in ingredientsForRecipe) {
        val inventoryItem = inventoryList.find {
            it.name.equals(ingredient.name, ignoreCase = true) &&
            it.unit.equals(ingredient.unit, ignoreCase = true)
        }
        if (inventoryItem != null) {
            if (inventoryItem.quantity >= ingredient.quantity) {
                val idx = inventoryList.indexOf(inventoryItem)
                inventoryList[idx] = inventoryItem.copy(quantity = inventoryItem.quantity - ingredient.quantity)
            } else {
                // Not enough in inventory: skip or warn
                println("Warning: Not enough ${ingredient.name} in inventory. Needed: ${ingredient.quantity} ${ingredient.unit}, Available: ${inventoryItem.quantity} ${inventoryItem.unit}")
            }
        } else {
            println("Warning: Ingredient ${ingredient.name} (${ingredient.unit}) not found in inventory.")
        }
    }
}
