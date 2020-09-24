package de.ovgu.ikus.model

enum class FoodTag {
    VEGAN,
    VEGETARIAN,
    GARLIC, // Knoblauch
    FISH,
    CHICKEN,
    BEEF,
    PIG,
    SOUP,
    ALCOHOL
}

data class Food(val name: String,
                val nameDe: String,
                val price: Double,
                val tags: List<FoodTag>)