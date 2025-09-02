package io.open_billing_store.util

import com.ibm.icu.text.NumberFormat
import com.ibm.icu.util.Currency
import com.ibm.icu.util.ULocale
import io.open_billing_store.entity.OrderSequence
import io.open_billing_store.repository.OrderSequenceRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class CommonUtils(
    private val orderSequenceRepository: OrderSequenceRepository
) {
    
    @Transactional
    fun generateOrderNumber(): String {
        val sequence = orderSequenceRepository.save(OrderSequence())
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"))
        return "ORD-$timestamp-${sequence.id.toString().padStart(6, '0')}"
    }
    
    fun getLocaleForCountry(countryCode: String): ULocale {
        return try {
            // Create ULocale with country code - ICU4J supports comprehensive country data
            ULocale.forLanguageTag("und-${countryCode.uppercase()}")
        } catch (e: Exception) {
            // Fallback to US locale if country code is invalid
            ULocale.US
        }
    }
    
    fun getCurrencyFormatterForCountry(countryCode: String, currencyCode: String): NumberFormat {
        val locale = getLocaleForCountry(countryCode)
        val formatter = NumberFormat.getCurrencyInstance(locale)
        
        return try {
            // Set the specific currency
            formatter.currency = Currency.getInstance(currencyCode)
            formatter
        } catch (e: Exception) {
            // If currency setting fails, return formatter with default currency for the locale
            formatter
        }
    }
}