package com.alejandro.minegociosv

/** Calculates practical unit-selling suggestions from an invoice purchase. */
data class PriceSuggestion(val costPerUnit: Double, val suggestedUnitsPerDollar: Double, val estimatedProfitPerDollar: Double)

object PriceSuggestionCalculator {
    fun calculate(totalCost: Double, units: Double, targetMarkupPercent: Double = 50.0): PriceSuggestion {
        require(totalCost > 0 && units > 0)
        val cost = totalCost / units
        val salePricePerUnit = cost * (1 + targetMarkupPercent / 100.0)
        val unitsPerDollar = 1.0 / salePricePerUnit
        val profit = 1.0 - unitsPerDollar * cost
        return PriceSuggestion(cost, unitsPerDollar, profit)
    }
}
