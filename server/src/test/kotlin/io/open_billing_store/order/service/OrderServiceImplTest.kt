package io.open_billing_store.order.service

import com.ibm.icu.text.NumberFormat
import com.ibm.icu.util.ULocale
import io.open_billing_store.calculate.PriceCalculator
import io.open_billing_store.calculate.ProductPriceResult
import io.open_billing_store.calculate.TaxCalculator
import io.open_billing_store.calculate.TaxCalculationResult
import io.open_billing_store.entity.*
import io.open_billing_store.order.request.OrderInitRequest
import io.open_billing_store.repository.OrderRepository
import io.open_billing_store.repository.ProductRepository
import io.open_billing_store.repository.UserRepository
import io.open_billing_store.util.CommonUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class OrderServiceImplTest {

    @Mock
    private lateinit var orderRepository: OrderRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var commonUtils: CommonUtils

    @Mock
    private lateinit var priceCalculator: PriceCalculator

    @Mock
    private lateinit var taxCalculator: TaxCalculator

    @InjectMocks
    private lateinit var orderService: OrderServiceImpl

    @Test
    fun `createOrder should create order successfully`() {
        // Given
        val request = OrderInitRequest(
            countryCode = "US",
            serviceId = "service123",
            productId = "product456",
            userId = "user789"
        )

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
            productId = "product456",
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
            countryCode = "US",
            countryName = "United States",
            price = BigDecimal("99.99"),
            currencyCode = "USD",
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val user = User(
            id = 1L,
            userId = "user789",
            service = service,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "123-456-7890",
            role = UserRole.CUSTOMER,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val country = Country(
            id = 1L,
            countryCode = "US",
            countryName = "United States",
            currencyCode = "USD",
            taxRate = BigDecimal("0.0875"),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val priceResult = ProductPriceResult(
            originalPrice = BigDecimal("99.99"),
            discountPercentage = BigDecimal.ZERO,
            discountAmount = BigDecimal.ZERO,
            finalPrice = BigDecimal("99.99"),
            currencyCode = "USD",
            productPriceByCountry = productPrice
        )

        val taxResult = TaxCalculationResult(
            baseAmount = BigDecimal("99.99"),
            taxRate = BigDecimal("0.0875"),
            taxAmount = BigDecimal("8.75"),
            totalAmount = BigDecimal("108.74"),
            country = country
        )

        val orderNumber = "ORD-2024010112-000001"
        val savedOrder = Order(
            id = 1L,
            orderNumber = orderNumber,
            user = user,
            service = service,
            product = product,
            subscription = null,
            currencyCode = "USD",
            productPrice = BigDecimal("99.99"),
            totalAmount = BigDecimal("108.74"),
            taxAmount = BigDecimal("8.75"),
            couponNumber = null,
            discountAmount = BigDecimal.ZERO,
            status = OrderStatus.PENDING,
            type = OrderType.ONE_TIME,
            dueDate = null,
            paidAt = null,
            billingAddress = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val currencyFormatter = NumberFormat.getCurrencyInstance(ULocale.US).apply {
            currency = com.ibm.icu.util.Currency.getInstance("USD")
        }

        `when`(productRepository.findByProductIdAndServiceServiceId("product456", "service123"))
            .thenReturn(product)
        `when`(userRepository.findByUserIdAndServiceServiceId("user789", "service123"))
            .thenReturn(Optional.of(user))
        `when`(priceCalculator.calculateProductPrice("product456", "US"))
            .thenReturn(priceResult)
        `when`(taxCalculator.calculateTaxAndTotalAmount(BigDecimal("99.99"), "US"))
            .thenReturn(taxResult)
        `when`(commonUtils.generateOrderNumber()).thenReturn(orderNumber)
        `when`(commonUtils.getCurrencyFormatterForCountry("US", "USD"))
            .thenReturn(currencyFormatter)
        `when`(orderRepository.save(any(Order::class.java))).thenReturn(savedOrder)

        // When
        val result = orderService.createOrder(request)

        // Then
        assertNotNull(result)
        assertEquals("SUCCESS", result.resultCode)
        assertEquals(orderNumber, result.orderId)
        assertEquals("99.99", result.productPrice)
        assertEquals("$99.99", result.displayProductPrice)
        assertEquals("8.75", result.taxAmount)
        assertEquals("$8.75", result.displayTaxAmount)
        assertEquals("108.74", result.totalPaymentAmount)
        assertEquals("$108.74", result.displayTotalPaymentAmount)
        assertEquals(false, result.isFreeTrial)
        assertEquals("USD", result.currencyCode)

        verify(productRepository).findByProductIdAndServiceServiceId("product456", "service123")
        verify(userRepository).findByUserIdAndServiceServiceId("user789", "service123")
        verify(priceCalculator).calculateProductPrice("product456", "US")
        verify(taxCalculator).calculateTaxAndTotalAmount(BigDecimal("99.99"), "US")
        verify(commonUtils).generateOrderNumber()
        verify(commonUtils).getCurrencyFormatterForCountry("US", "USD")
        verify(orderRepository).save(any(Order::class.java))
    }

    @Test
    fun `createOrder should create subscription order for subscription product`() {
        // Given
        val request = OrderInitRequest(
            countryCode = "US",
            serviceId = "service123",
            productId = "subscription456",
            userId = "user789"
        )

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
            productId = "subscription456",
            service = service,
            name = "Subscription Product",
            description = "Monthly Subscription",
            imageUrl = "http://example.com/image.jpg",
            category = null,
            isActive = true,
            type = ProductType.SUBSCRIPTION_SERVICE,
            billingInterval = BillingInterval.MONTHLY,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val productPrice = ProductPriceByCountry(
            id = 1L,
            product = product,
            countryCode = "US",
            countryName = "United States",
            price = BigDecimal("29.99"),
            currencyCode = "USD",
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val user = User(
            id = 1L,
            userId = "user789",
            service = service,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "123-456-7890",
            role = UserRole.CUSTOMER,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val country = Country(
            id = 1L,
            countryCode = "US",
            countryName = "United States",
            currencyCode = "USD",
            taxRate = BigDecimal("0.0875"),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val priceResult = ProductPriceResult(
            originalPrice = BigDecimal("29.99"),
            discountPercentage = BigDecimal.ZERO,
            discountAmount = BigDecimal.ZERO,
            finalPrice = BigDecimal("29.99"),
            currencyCode = "USD",
            productPriceByCountry = productPrice
        )

        val taxResult = TaxCalculationResult(
            baseAmount = BigDecimal("29.99"),
            taxRate = BigDecimal("0.0875"),
            taxAmount = BigDecimal("2.62"),
            totalAmount = BigDecimal("32.61"),
            country = country
        )

        val orderNumber = "ORD-2024010112-000002"
        val savedOrder = Order(
            id = 2L,
            orderNumber = orderNumber,
            user = user,
            service = service,
            product = product,
            subscription = null,
            currencyCode = "USD",
            productPrice = BigDecimal("29.99"),
            totalAmount = BigDecimal("32.61"),
            taxAmount = BigDecimal("2.62"),
            couponNumber = null,
            discountAmount = BigDecimal.ZERO,
            status = OrderStatus.PENDING,
            type = OrderType.SUBSCRIPTION_BILLING,
            dueDate = null,
            paidAt = null,
            billingAddress = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val currencyFormatter = NumberFormat.getCurrencyInstance(ULocale.US).apply {
            currency = com.ibm.icu.util.Currency.getInstance("USD")
        }

        `when`(productRepository.findByProductIdAndServiceServiceId("subscription456", "service123"))
            .thenReturn(product)
        `when`(userRepository.findByUserIdAndServiceServiceId("user789", "service123"))
            .thenReturn(Optional.of(user))
        `when`(priceCalculator.calculateProductPrice("subscription456", "US"))
            .thenReturn(priceResult)
        `when`(taxCalculator.calculateTaxAndTotalAmount(BigDecimal("29.99"), "US"))
            .thenReturn(taxResult)
        `when`(commonUtils.generateOrderNumber()).thenReturn(orderNumber)
        `when`(commonUtils.getCurrencyFormatterForCountry("US", "USD"))
            .thenReturn(currencyFormatter)
        `when`(orderRepository.save(any(Order::class.java))).thenReturn(savedOrder)

        // When
        val result = orderService.createOrder(request)

        // Then
        assertNotNull(result)
        assertEquals("SUCCESS", result.resultCode)
        assertEquals(orderNumber, result.orderId)
        assertEquals("29.99", result.productPrice)
        assertEquals("$29.99", result.displayProductPrice)
        assertEquals("USD", result.currencyCode)

        verify(orderRepository).save(any(Order::class.java))
    }

    @Test
    fun `createOrder should handle free trial correctly`() {
        // Given
        val request = OrderInitRequest(
            countryCode = "US",
            serviceId = "service123",
            productId = "free456",
            userId = "user789"
        )

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
            productId = "free456",
            service = service,
            name = "Free Trial Product",
            description = "Free Trial",
            imageUrl = "http://example.com/image.jpg",
            category = null,
            isActive = true,
            type = ProductType.SUBSCRIPTION_SERVICE,
            billingInterval = BillingInterval.MONTHLY,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val productPrice = ProductPriceByCountry(
            id = 1L,
            product = product,
            countryCode = "US",
            countryName = "United States",
            price = BigDecimal.ZERO,
            currencyCode = "USD",
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val user = User(
            id = 1L,
            userId = "user789",
            service = service,
            email = "test@example.com",
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "123-456-7890",
            role = UserRole.CUSTOMER,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val country = Country(
            id = 1L,
            countryCode = "US",
            countryName = "United States",
            currencyCode = "USD",
            taxRate = BigDecimal("0.0875"),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val priceResult = ProductPriceResult(
            originalPrice = BigDecimal.ZERO,
            discountPercentage = BigDecimal.ZERO,
            discountAmount = BigDecimal.ZERO,
            finalPrice = BigDecimal.ZERO,
            currencyCode = "USD",
            productPriceByCountry = productPrice
        )

        val taxResult = TaxCalculationResult(
            baseAmount = BigDecimal.ZERO,
            taxRate = BigDecimal("0.0875"),
            taxAmount = BigDecimal.ZERO,
            totalAmount = BigDecimal.ZERO,
            country = country
        )

        val orderNumber = "ORD-2024010112-000003"
        val savedOrder = Order(
            id = 3L,
            orderNumber = orderNumber,
            user = user,
            service = service,
            product = product,
            subscription = null,
            currencyCode = "USD",
            productPrice = BigDecimal.ZERO,
            totalAmount = BigDecimal.ZERO,
            taxAmount = BigDecimal.ZERO,
            couponNumber = null,
            discountAmount = BigDecimal.ZERO,
            status = OrderStatus.PENDING,
            type = OrderType.SUBSCRIPTION_BILLING,
            dueDate = null,
            paidAt = null,
            billingAddress = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val currencyFormatter = NumberFormat.getCurrencyInstance(ULocale.US).apply {
            currency = com.ibm.icu.util.Currency.getInstance("USD")
        }

        `when`(productRepository.findByProductIdAndServiceServiceId("free456", "service123"))
            .thenReturn(product)
        `when`(userRepository.findByUserIdAndServiceServiceId("user789", "service123"))
            .thenReturn(Optional.of(user))
        `when`(priceCalculator.calculateProductPrice("free456", "US"))
            .thenReturn(priceResult)
        `when`(taxCalculator.calculateTaxAndTotalAmount(BigDecimal.ZERO, "US"))
            .thenReturn(taxResult)
        `when`(commonUtils.generateOrderNumber()).thenReturn(orderNumber)
        `when`(commonUtils.getCurrencyFormatterForCountry("US", "USD"))
            .thenReturn(currencyFormatter)
        `when`(orderRepository.save(any(Order::class.java))).thenReturn(savedOrder)

        // When
        val result = orderService.createOrder(request)

        // Then
        assertNotNull(result)
        assertEquals("SUCCESS", result.resultCode)
        assertEquals(true, result.isFreeTrial)
        assertEquals("0", result.productPrice)
        assertEquals("0", result.taxAmount)
        assertEquals("0", result.totalPaymentAmount)
    }

    @Test
    fun `createOrder should throw exception when product not found`() {
        // Given
        val request = OrderInitRequest(
            countryCode = "US",
            serviceId = "service123",
            productId = "nonexistent",
            userId = "user789"
        )

        `when`(productRepository.findByProductIdAndServiceServiceId("nonexistent", "service123"))
            .thenReturn(null)

        // When & Then
        val exception = assertThrows(RuntimeException::class.java) {
            orderService.createOrder(request)
        }

        assertEquals("Product not found with id: nonexistent and service: service123", exception.message)
        verify(productRepository).findByProductIdAndServiceServiceId("nonexistent", "service123")
    }

    @Test
    fun `createOrder should throw exception when user not found`() {
        // Given
        val request = OrderInitRequest(
            countryCode = "US",
            serviceId = "service123",
            productId = "product456",
            userId = "nonexistent"
        )

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
            productId = "product456",
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

        `when`(productRepository.findByProductIdAndServiceServiceId("product456", "service123"))
            .thenReturn(product)
        `when`(userRepository.findByUserIdAndServiceServiceId("nonexistent", "service123"))
            .thenReturn(Optional.empty())

        // When & Then
        val exception = assertThrows(RuntimeException::class.java) {
            orderService.createOrder(request)
        }

        assertEquals("User not found with userId: nonexistent and serviceId: service123", exception.message)
        verify(productRepository).findByProductIdAndServiceServiceId("product456", "service123")
        verify(userRepository).findByUserIdAndServiceServiceId("nonexistent", "service123")
    }
}