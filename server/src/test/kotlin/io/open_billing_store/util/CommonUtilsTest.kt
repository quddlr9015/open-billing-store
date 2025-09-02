package io.open_billing_store.util

import com.ibm.icu.text.NumberFormat
import com.ibm.icu.util.ULocale
import io.open_billing_store.entity.OrderSequence
import io.open_billing_store.repository.OrderSequenceRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
class CommonUtilsTest {

    @Mock
    private lateinit var orderSequenceRepository: OrderSequenceRepository

    @InjectMocks
    private lateinit var commonUtils: CommonUtils

    @Test
    fun `generateOrderNumber should create unique order number with correct format`() {
        // Given
        val mockSequence = OrderSequence(id = 123L)
        `when`(orderSequenceRepository.save(any(OrderSequence::class.java))).thenReturn(mockSequence)

        // When
        val result = commonUtils.generateOrderNumber()

        // Then
        assertNotNull(result)
        assertTrue(result.startsWith("ORD-"))
        assertTrue(result.endsWith("-000123"))
        assertEquals(21, result.length) // ORD-YYYYMMDDHH-XXXXXX = 21 characters
        
        // Verify format: ORD-2024010112-000123
        val parts = result.split("-")
        assertEquals(3, parts.size)
        assertEquals("ORD", parts[0])
        assertEquals(10, parts[1].length) // YYYYMMDDHH
        assertEquals(6, parts[2].length) // XXXXXX with leading zeros
        
        verify(orderSequenceRepository).save(any(OrderSequence::class.java))
    }

    @Test
    fun `generateOrderNumber should pad sequence number with leading zeros`() {
        // Given
        val mockSequence = OrderSequence(id = 42L)
        `when`(orderSequenceRepository.save(any(OrderSequence::class.java))).thenReturn(mockSequence)

        // When
        val result = commonUtils.generateOrderNumber()

        // Then
        assertTrue(result.endsWith("-000042"))
        verify(orderSequenceRepository).save(any(OrderSequence::class.java))
    }

    @Test
    fun `generateOrderNumber should handle large sequence numbers`() {
        // Given
        val mockSequence = OrderSequence(id = 999999L)
        `when`(orderSequenceRepository.save(any(OrderSequence::class.java))).thenReturn(mockSequence)

        // When
        val result = commonUtils.generateOrderNumber()

        // Then
        assertTrue(result.endsWith("-999999"))
        verify(orderSequenceRepository).save(any(OrderSequence::class.java))
    }

    @Test
    fun `getLocaleForCountry should return correct ULocale for valid country codes`() {
        // Test various country codes
        val testCases = mapOf(
            "US" to ULocale.forLanguageTag("und-US"),
            "CA" to ULocale.forLanguageTag("und-CA"),
            "GB" to ULocale.forLanguageTag("und-GB"),
            "DE" to ULocale.forLanguageTag("und-DE"),
            "FR" to ULocale.forLanguageTag("und-FR"),
            "JP" to ULocale.forLanguageTag("und-JP"),
            "KR" to ULocale.forLanguageTag("und-KR")
        )

        testCases.forEach { (countryCode, expectedLocale) ->
            // When
            val result = commonUtils.getLocaleForCountry(countryCode)

            // Then
            assertEquals(expectedLocale.country, result.country)
            assertNotNull(result)
        }
    }

    @Test
    fun `getLocaleForCountry should handle lowercase country codes`() {
        // Given
        val countryCode = "us"

        // When
        val result = commonUtils.getLocaleForCountry(countryCode)

        // Then
        assertEquals("US", result.country)
    }

    @Test
    fun `getLocaleForCountry should handle invalid country codes gracefully`() {
        // Given
        val invalidCountryCode = "XX"

        // When
        val result = commonUtils.getLocaleForCountry(invalidCountryCode)

        // Then
        assertNotNull(result)
        assertEquals("XX", result.country)
    }

    @Test
    fun `getLocaleForCountry should handle edge cases`() {
        // Test with empty string - should create a locale but might not be ideal
        val emptyResult = commonUtils.getLocaleForCountry("")
        assertNotNull(emptyResult)
        
        // Test with very long invalid string - should still work
        val longResult = commonUtils.getLocaleForCountry("INVALID_VERY_LONG_COUNTRY_CODE")
        assertNotNull(longResult)
    }

    @Test
    fun `getCurrencyFormatterForCountry should return correct formatter for US`() {
        // Given
        val countryCode = "US"
        val currencyCode = "USD"

        // When
        val result = commonUtils.getCurrencyFormatterForCountry(countryCode, currencyCode)

        // Then
        assertNotNull(result)
        assertEquals("USD", result.currency.currencyCode)
        
        // Test formatting
        val formatted = result.format(BigDecimal("99.99"))
        assertTrue(formatted.contains("99.99"))
        assertTrue(formatted.contains("$") || formatted.contains("USD"))
    }

    @Test
    fun `getCurrencyFormatterForCountry should return correct formatter for EUR`() {
        // Given
        val countryCode = "DE"
        val currencyCode = "EUR"

        // When
        val result = commonUtils.getCurrencyFormatterForCountry(countryCode, currencyCode)

        // Then
        assertNotNull(result)
        assertEquals("EUR", result.currency.currencyCode)
        
        // Test formatting
        val formatted = result.format(BigDecimal("99.99"))
        assertTrue(formatted.contains("99") || formatted.contains("100"))
        assertTrue(formatted.contains("€") || formatted.contains("EUR"))
    }

    @Test
    fun `getCurrencyFormatterForCountry should return correct formatter for JPY`() {
        // Given
        val countryCode = "JP"
        val currencyCode = "JPY"

        // When
        val result = commonUtils.getCurrencyFormatterForCountry(countryCode, currencyCode)

        // Then
        assertNotNull(result)
        assertEquals("JPY", result.currency.currencyCode)
        
        // Test formatting - JPY typically doesn't show decimal places
        val formatted = result.format(BigDecimal("99.99"))
        assertTrue(formatted.contains("99") || formatted.contains("100"))
        assertTrue(formatted.contains("¥") || formatted.contains("JPY"))
    }

    @Test
    fun `getCurrencyFormatterForCountry should handle invalid currency gracefully`() {
        // Given
        val countryCode = "US"
        val invalidCurrencyCode = "XXX"

        // When
        val result = commonUtils.getCurrencyFormatterForCountry(countryCode, invalidCurrencyCode)

        // Then
        assertNotNull(result)
        // Should still return a formatter even if currency setting fails
        val formatted = result.format(BigDecimal("99.99"))
        assertNotNull(formatted)
    }

    @Test
    fun `getCurrencyFormatterForCountry should format different amounts correctly`() {
        // Given
        val countryCode = "US"
        val currencyCode = "USD"
        val formatter = commonUtils.getCurrencyFormatterForCountry(countryCode, currencyCode)

        val testCases = mapOf(
            BigDecimal("0.00") to "0.00",
            BigDecimal("1.00") to "1.00",
            BigDecimal("99.99") to "99.99",
            BigDecimal("1000.00") to "1,000.00",
            BigDecimal("1234567.89") to "1,234,567.89"
        )

        testCases.forEach { (amount, expectedPattern) ->
            // When
            val formatted = formatter.format(amount)

            // Then
            assertTrue(formatted.contains(expectedPattern.replace(",", "")) || formatted.contains(expectedPattern))
        }
    }

    @Test
    fun `getCurrencyFormatterForCountry should handle zero amounts`() {
        // Given
        val countryCode = "US"
        val currencyCode = "USD"

        // When
        val formatter = commonUtils.getCurrencyFormatterForCountry(countryCode, currencyCode)
        val formatted = formatter.format(BigDecimal.ZERO)

        // Then
        assertNotNull(formatted)
        assertTrue(formatted.contains("0"))
    }

    @Test
    fun `getCurrencyFormatterForCountry should handle negative amounts`() {
        // Given
        val countryCode = "US"
        val currencyCode = "USD"

        // When
        val formatter = commonUtils.getCurrencyFormatterForCountry(countryCode, currencyCode)
        val formatted = formatter.format(BigDecimal("-99.99"))

        // Then
        assertNotNull(formatted)
        assertTrue(formatted.contains("99.99"))
        assertTrue(formatted.contains("-") || formatted.contains("("))
    }

    @Test
    fun `getCurrencyFormatterForCountry should work with different country-currency combinations`() {
        val testCases = listOf(
            Triple("CA", "CAD", BigDecimal("99.99")),
            Triple("GB", "GBP", BigDecimal("99.99")),
            Triple("AU", "AUD", BigDecimal("99.99")),
            Triple("IN", "INR", BigDecimal("99.99")),
            Triple("BR", "BRL", BigDecimal("99.99"))
        )

        testCases.forEach { (countryCode, currencyCode, amount) ->
            // When
            val formatter = commonUtils.getCurrencyFormatterForCountry(countryCode, currencyCode)
            val formatted = formatter.format(amount)

            // Then
            assertNotNull(formatter)
            assertNotNull(formatted)
            assertEquals(currencyCode, formatter.currency.currencyCode)
        }
    }
}