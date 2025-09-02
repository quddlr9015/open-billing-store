package io.open_billing_store.calculate

import io.open_billing_store.entity.Country
import io.open_billing_store.repository.CountryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class TaxCalculatorImplTest {

    @Mock
    private lateinit var countryRepository: CountryRepository

    @InjectMocks
    private lateinit var taxCalculator: TaxCalculatorImpl

    @Test
    fun `calculateTaxAndTotalAmount should calculate tax correctly for US`() {
        // Given
        val amount = BigDecimal("100.00")
        val countryCode = "US"
        
        val country = Country(
            id = 1L,
            countryCode = countryCode,
            countryName = "United States",
            stateCode = null,
            stateName = null,
            currencyCode = "USD",
            taxRate = BigDecimal("0.0875"), // 8.75% tax rate
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(countryRepository.findByCountryCodeAndIsActiveTrue(countryCode)).thenReturn(country)

        // When
        val result = taxCalculator.calculateTaxAndTotalAmount(amount, countryCode)

        // Then
        assertEquals(amount, result.baseAmount)
        assertEquals(0, BigDecimal("0.0875").compareTo(result.taxRate))
        assertEquals(0, BigDecimal("8.75").compareTo(result.taxAmount))
        assertEquals(0, BigDecimal("108.75").compareTo(result.totalAmount))
        assertEquals(country, result.country)

        verify(countryRepository).findByCountryCodeAndIsActiveTrue(countryCode)
    }

    @Test
    fun `calculateTaxAndTotalAmount should calculate tax correctly for zero tax rate`() {
        // Given
        val amount = BigDecimal("100.00")
        val countryCode = "DE"
        
        val country = Country(
            id = 2L,
            countryCode = countryCode,
            countryName = "Germany",
            stateCode = null,
            stateName = null,
            currencyCode = "EUR",
            taxRate = BigDecimal("0.0000"), // 0% tax rate
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(countryRepository.findByCountryCodeAndIsActiveTrue(countryCode)).thenReturn(country)

        // When
        val result = taxCalculator.calculateTaxAndTotalAmount(amount, countryCode)

        // Then
        assertEquals(amount, result.baseAmount)
        assertEquals(0, BigDecimal("0.0000").compareTo(result.taxRate))
        assertEquals(0, BigDecimal("0.00").compareTo(result.taxAmount))
        assertEquals(0, BigDecimal("100.00").compareTo(result.totalAmount))
        assertEquals(country, result.country)

        verify(countryRepository).findByCountryCodeAndIsActiveTrue(countryCode)
    }

    @Test
    fun `calculateTaxAndTotalAmount should fallback to US when country not found`() {
        // Given
        val amount = BigDecimal("100.00")
        val countryCode = "XX"
        
        val usCountry = Country(
            id = 1L,
            countryCode = "US",
            countryName = "United States",
            stateCode = null,
            stateName = null,
            currencyCode = "USD",
            taxRate = BigDecimal("0.0875"), // 8.75% tax rate
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(countryRepository.findByCountryCodeAndIsActiveTrue(countryCode)).thenReturn(null)
        `when`(countryRepository.findByCountryCodeAndIsActiveTrue("US")).thenReturn(usCountry)

        // When
        val result = taxCalculator.calculateTaxAndTotalAmount(amount, countryCode)

        // Then
        assertEquals(amount, result.baseAmount)
        assertEquals(0, BigDecimal("0.0875").compareTo(result.taxRate))
        assertEquals(0, BigDecimal("8.75").compareTo(result.taxAmount))
        assertEquals(0, BigDecimal("108.75").compareTo(result.totalAmount))
        assertEquals(usCountry, result.country)

        verify(countryRepository).findByCountryCodeAndIsActiveTrue(countryCode)
        verify(countryRepository).findByCountryCodeAndIsActiveTrue("US")
    }

    @Test
    fun `calculateTaxAndTotalAmount should throw exception when no country found`() {
        // Given
        val amount = BigDecimal("100.00")
        val countryCode = "XX"

        `when`(countryRepository.findByCountryCodeAndIsActiveTrue(countryCode)).thenReturn(null)
        `when`(countryRepository.findByCountryCodeAndIsActiveTrue("US")).thenReturn(null)

        // When & Then
        val exception = assertThrows(RuntimeException::class.java) {
            taxCalculator.calculateTaxAndTotalAmount(amount, countryCode)
        }

        assertEquals("Tax information not found for country: $countryCode", exception.message)
        
        verify(countryRepository).findByCountryCodeAndIsActiveTrue(countryCode)
        verify(countryRepository).findByCountryCodeAndIsActiveTrue("US")
    }

    @Test
    fun `calculateTaxAndTotalAmount should handle high tax rate correctly`() {
        // Given
        val amount = BigDecimal("50.00")
        val countryCode = "SE"
        
        val country = Country(
            id = 3L,
            countryCode = countryCode,
            countryName = "Sweden",
            stateCode = null,
            stateName = null,
            currencyCode = "SEK",
            taxRate = BigDecimal("0.25"), // 25% tax rate
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(countryRepository.findByCountryCodeAndIsActiveTrue(countryCode)).thenReturn(country)

        // When
        val result = taxCalculator.calculateTaxAndTotalAmount(amount, countryCode)

        // Then
        assertEquals(amount, result.baseAmount)
        assertEquals(0, BigDecimal("0.25").compareTo(result.taxRate))
        assertEquals(0, BigDecimal("12.50").compareTo(result.taxAmount))
        assertEquals(0, BigDecimal("62.50").compareTo(result.totalAmount))
        assertEquals(country, result.country)

        verify(countryRepository).findByCountryCodeAndIsActiveTrue(countryCode)
    }

    @Test
    fun `calculateTaxAndTotalAmount should handle small amounts correctly`() {
        // Given
        val amount = BigDecimal("1.00")
        val countryCode = "CA"
        
        val country = Country(
            id = 4L,
            countryCode = countryCode,
            countryName = "Canada",
            stateCode = null,
            stateName = null,
            currencyCode = "CAD",
            taxRate = BigDecimal("0.13"), // 13% tax rate
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(countryRepository.findByCountryCodeAndIsActiveTrue(countryCode)).thenReturn(country)

        // When
        val result = taxCalculator.calculateTaxAndTotalAmount(amount, countryCode)

        // Then
        assertEquals(amount, result.baseAmount)
        assertEquals(0, BigDecimal("0.13").compareTo(result.taxRate))
        assertEquals(0, BigDecimal("0.13").compareTo(result.taxAmount))
        assertEquals(0, BigDecimal("1.13").compareTo(result.totalAmount))
        assertEquals(country, result.country)

        verify(countryRepository).findByCountryCodeAndIsActiveTrue(countryCode)
    }
}