package io.open_billing_store.calculate

import java.math.BigDecimal

interface TaxCalculator {
    fun calculateTaxAndTotalAmount(amount: BigDecimal, countryCode: String): TaxCalculationResult
}