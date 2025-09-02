package io.open_billing_store.calculate

import io.open_billing_store.entity.Country
import io.open_billing_store.repository.CountryRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class TaxCalculatorImpl(
    private val countryRepository: CountryRepository
) : TaxCalculator {
    
    override fun calculateTaxAndTotalAmount(amount: BigDecimal, countryCode: String): TaxCalculationResult {
        val country = getCountryForTaxCalculation(countryCode)
        val taxRate = country.taxRate
        val taxAmount = amount.multiply(taxRate)
        
        return TaxCalculationResult(
            baseAmount = amount,
            taxRate = taxRate,
            taxAmount = taxAmount,
            totalAmount = amount.add(taxAmount),
            country = country
        )
    }
    
    private fun getCountryForTaxCalculation(countryCode: String): Country {
        return countryRepository.findByCountryCodeAndIsActiveTrue(countryCode)
            ?: countryRepository.findByCountryCodeAndIsActiveTrue("US") // fallback to US
            ?: throw RuntimeException("Tax information not found for country: $countryCode")
    }
}

data class TaxCalculationResult(
    val baseAmount: BigDecimal,
    val taxRate: BigDecimal,
    val taxAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val country: Country
)