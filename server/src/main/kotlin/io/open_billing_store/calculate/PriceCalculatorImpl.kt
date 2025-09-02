package io.open_billing_store.calculate

import io.open_billing_store.entity.ProductPriceByCountry
import io.open_billing_store.repository.ProductPriceByCountryRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PriceCalculatorImpl(
    private val productPriceByCountryRepository: ProductPriceByCountryRepository
) : PriceCalculator {
    
    override fun calculateProductPrice(productId: String, countryCode: String): ProductPriceResult {
        val productPrice = getProductPriceByCountry(productId, countryCode)
        val basePrice = productPrice.discountedPrice ?: productPrice.price
        val originalPrice = productPrice.price
        val discount = productPrice.discountPercentage ?: BigDecimal.ZERO
        val discountAmount = if (productPrice.discountedPrice != null) {
            originalPrice.subtract(productPrice.discountedPrice)
        } else {
            BigDecimal.ZERO
        }
        
        return ProductPriceResult(
            originalPrice = originalPrice,
            discountPercentage = discount,
            discountAmount = discountAmount,
            finalPrice = basePrice,
            currencyCode = productPrice.currencyCode,
            productPriceByCountry = productPrice
        )
    }
    
    private fun getProductPriceByCountry(productId: String, countryCode: String): ProductPriceByCountry {
        return productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            productId,
            countryCode
        ) ?: productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            productId,
            "US"
        ) ?: throw RuntimeException("Product price not found for product: $productId in any supported country")
    }
}

data class ProductPriceResult(
    val originalPrice: BigDecimal,
    val discountPercentage: BigDecimal,
    val discountAmount: BigDecimal,
    val finalPrice: BigDecimal,
    val currencyCode: String,
    val productPriceByCountry: ProductPriceByCountry
)