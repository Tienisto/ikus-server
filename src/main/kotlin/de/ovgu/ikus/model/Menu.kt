package de.ovgu.ikus.model

import java.time.LocalDate

data class Menu(val date: LocalDate,
                val food: List<Food>)