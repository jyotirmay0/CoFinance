package com.finance.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.finance.app.domain.model.Category

fun Category.toColor(): Color = when (this) {
    Category.FOOD         -> CategoryFood
    Category.TRAVEL       -> CategoryTravel
    Category.BILLS        -> CategoryBills
    Category.SHOPPING     -> CategoryShopping
    Category.ENTERTAINMENT -> CategoryEntertainment
    Category.HEALTH       -> CategoryHealth
    Category.TRANSPORT    -> CategoryTransport
    Category.HOUSING      -> CategoryHousing
    Category.FITNESS      -> CategoryFitness
    Category.SUBSCRIPTION -> CategorySubscription
    Category.OTHER        -> CategoryOther
    Category.SALARY       -> CategorySalary
    Category.FREELANCE    -> CategoryFreelance
    Category.INVESTMENT   -> CategoryInvestment
    Category.INCOME_OTHER -> CategoryIncomeOther
}