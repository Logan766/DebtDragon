package tech.janhoracek.debtdragon.utility

import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized

fun transformCategoryToDatabaseString(category: String):String {
    return when(category) {
        localized(R.string.category_food) -> Constants.DATABASE_DEBT_CATEGORY_FOOD
        localized(R.string.category_entertainment) -> Constants.DATABASE_DEBT_CATEGORY_ENTERTAINMENT
        localized(R.string.categroy_finance) -> Constants.DATABASE_DEBT_CATEGORY_FINANCE
        localized(R.string.category_clothing_access) -> Constants.DATABASE_DEBT_CATEGORY_FASHION
        localized(R.string.category_electronics) -> Constants.DATABASE_DEBT_CATEGORY_ELECTRONIC
        localized(R.string.category_other) -> Constants.DATABASE_DEBT_CATEGORY_OTHER
        localized(R.string.category_payment) -> Constants.DATABASE_DEBT_CATEGORY_PAYMENT
        else -> {
            "Unknown"
        }
    }
}

fun transformDatabaseStringToCategory(category: String):String {
    return when(category) {
        Constants.DATABASE_DEBT_CATEGORY_FOOD -> localized(R.string.category_food)
        Constants.DATABASE_DEBT_CATEGORY_ENTERTAINMENT -> localized(R.string.category_entertainment)
        Constants.DATABASE_DEBT_CATEGORY_FINANCE -> localized(R.string.categroy_finance)
        Constants.DATABASE_DEBT_CATEGORY_FASHION -> localized(R.string.category_clothing_access)
        Constants.DATABASE_DEBT_CATEGORY_ELECTRONIC -> localized(R.string.category_electronics)
        Constants.DATABASE_DEBT_CATEGORY_OTHER -> localized(R.string.category_other)
        Constants.DATABASE_DEBT_CATEGORY_PAYMENT -> localized(R.string.category_payment)
        Constants.DATABASE_DEBT_CATEGORY_GDEBT -> localized(R.string.category_g_debts)
        else -> {
            "Unknown"
        }
    }
}


