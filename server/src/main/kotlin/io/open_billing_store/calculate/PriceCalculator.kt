package io.open_billing_store.calculate

interface PriceCalculator {
    fun calculateProductPrice(productId: String, countryCode: String): ProductPriceResult
}