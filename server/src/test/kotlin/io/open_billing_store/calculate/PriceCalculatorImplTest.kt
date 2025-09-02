package io.open_billing_store.calculate

import io.open_billing_store.entity.*
import io.open_billing_store.repository.ProductPriceByCountryRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PriceCalculatorImplTest {

    @Mock
    private lateinit var productPriceByCountryRepository: ProductPriceByCountryRepository

    @InjectMocks
    private lateinit var priceCalculator: PriceCalculatorImpl

    @Test
    fun `calculateProductPrice should return correct price without discount`() {
        // Given
        val productId = "PROD001"
        val countryCode = "US"
        
        val service = Service(
            serviceId = "service123",
            serviceName = "Test Service",
            description = "Test Description",
            apiKey = "test-api-key",
            status = ServiceStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val product = Product(
            productId = productId,
            service = service,
            name = "Test Product",
            description = "Test Product Description",
            imageUrl = "http://example.com/image.jpg",
            category = null,
            isActive = true,
            type = ProductType.ONE_TIME_SERVICE,
            billingInterval = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val productPrice = ProductPriceByCountry(
            id = 1L,
            product = product,
            countryCode = countryCode,
            countryName = "United States",
            price = BigDecimal("99.99"),
            currencyCode = "USD",
            discountPercentage = null,
            discountedPrice = null,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )).thenReturn(productPrice)

        // When
        val result = priceCalculator.calculateProductPrice(productId, countryCode)

        // Then
        assertEquals(BigDecimal("99.99"), result.originalPrice)
        assertEquals(BigDecimal.ZERO, result.discountPercentage)
        assertEquals(BigDecimal.ZERO, result.discountAmount)
        assertEquals(BigDecimal("99.99"), result.finalPrice)
        assertEquals("USD", result.currencyCode)
        assertEquals(productPrice, result.productPriceByCountry)

        verify(productPriceByCountryRepository).findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )
    }

    @Test
    fun `calculateProductPrice should return correct price with discount`() {
        // Given
        val productId = "PROD001"
        val countryCode = "US"
        
        val service = Service(
            serviceId = "service123",
            serviceName = "Test Service",
            description = "Test Description",
            apiKey = "test-api-key",
            status = ServiceStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val product = Product(
            productId = productId,
            service = service,
            name = "Test Product",
            description = "Test Product Description",
            imageUrl = "http://example.com/image.jpg",
            category = null,
            isActive = true,
            type = ProductType.ONE_TIME_SERVICE,
            billingInterval = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val productPrice = ProductPriceByCountry(
            id = 1L,
            product = product,
            countryCode = countryCode,
            countryName = "United States",
            price = BigDecimal("100.00"),
            currencyCode = "USD",
            discountPercentage = BigDecimal("20.00"),
            discountedPrice = BigDecimal("80.00"),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )).thenReturn(productPrice)

        // When
        val result = priceCalculator.calculateProductPrice(productId, countryCode)

        // Then
        assertEquals(BigDecimal("100.00"), result.originalPrice)
        assertEquals(BigDecimal("20.00"), result.discountPercentage)
        assertEquals(BigDecimal("20.00"), result.discountAmount)
        assertEquals(BigDecimal("80.00"), result.finalPrice)
        assertEquals("USD", result.currencyCode)
        assertEquals(productPrice, result.productPriceByCountry)

        verify(productPriceByCountryRepository).findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )
    }

    @Test
    fun `calculateProductPrice should fallback to US when country not found`() {
        // Given
        val productId = "PROD001"
        val countryCode = "XX"
        
        val service = Service(
            serviceId = "service123",
            serviceName = "Test Service",
            description = "Test Description",
            apiKey = "test-api-key",
            status = ServiceStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val product = Product(
            productId = productId,
            service = service,
            name = "Test Product",
            description = "Test Product Description",
            imageUrl = "http://example.com/image.jpg",
            category = null,
            isActive = true,
            type = ProductType.ONE_TIME_SERVICE,
            billingInterval = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val usPricing = ProductPriceByCountry(
            id = 1L,
            product = product,
            countryCode = "US",
            countryName = "United States",
            price = BigDecimal("99.99"),
            currencyCode = "USD",
            discountPercentage = null,
            discountedPrice = null,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )).thenReturn(null)
        `when`(productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq("US"), 
            any()
        )).thenReturn(usPricing)

        // When
        val result = priceCalculator.calculateProductPrice(productId, countryCode)

        // Then
        assertEquals(BigDecimal("99.99"), result.originalPrice)
        assertEquals(BigDecimal("99.99"), result.finalPrice)
        assertEquals("USD", result.currencyCode)

        verify(productPriceByCountryRepository).findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )
        verify(productPriceByCountryRepository).findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq("US"), 
            any()
        )
    }

    @Test
    fun `calculateProductPrice should throw exception when no pricing found`() {
        // Given
        val productId = "PROD001"
        val countryCode = "XX"

        `when`(productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )).thenReturn(null)
        `when`(productPriceByCountryRepository.findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq("US"), 
            any()
        )).thenReturn(null)

        // When & Then
        val exception = assertThrows(RuntimeException::class.java) {
            priceCalculator.calculateProductPrice(productId, countryCode)
        }

        assertEquals("Product price not found for product: $productId in any supported country", exception.message)
        
        verify(productPriceByCountryRepository).findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq(countryCode), 
            any()
        )
        verify(productPriceByCountryRepository).findActiveProductPriceByProductIdAndCountryCode(
            eq(productId), 
            eq("US"), 
            any()
        )
    }
}