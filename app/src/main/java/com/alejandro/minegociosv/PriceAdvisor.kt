package com.alejandro.minegociosv

import kotlin.math.ceil

/** Converts purchase cost into a suggested retail quantity per $1. */
object PriceAdvisor {
    fun costPerUnit(totalCost: Double, units: Double): Double =
        if (units > 0) totalCost / units else 0.0

    fun suggestedUnitsPerDollar(totalCost: Double, units: Double, markupPercent: Double = 30.0): Double {
        val cost = costPerUnit(totalCost, units)
        if (cost <= 0) return 0.0
        return 1.0 / (cost * (1.0 + markupPercent / 100.0))
    }

    fun roundedUnitsPerDollar(totalCost: Double, units: Double, markupPercent: Double = 30.0): Int {
        val suggestion = suggestedUnitsPerDollar(totalCost, units, markupPercent)
        return if (suggestion > 0) ceil(suggestion).toInt() else 0
    }
}
