package io.open_billing_store.order.service

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
            productPrice = BigDecimal("99.99"),
            imageUrl = "http://example.com/image.jpg",
            category = null,
            isActive = true,
            type = ProductType.ONE_TIME_SERVICE,
            billingInterval = null,
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
            totalAmount = BigDecimal("99.99"),
            taxAmount = BigDecimal.ZERO,
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

        `when`(productRepository.findByProductIdAndServiceServiceId("product456", "service123"))
            .thenReturn(product)
        `when`(userRepository.findByUserIdAndServiceServiceId("user789", "service123"))
            .thenReturn(Optional.of(user))
        `when`(commonUtils.generateOrderNumber()).thenReturn(orderNumber)
        `when`(orderRepository.save(any(Order::class.java))).thenReturn(savedOrder)

        // When
        val result = orderService.createOrder(request)

        // Then
        assertNotNull(result)
        assertEquals(orderNumber, result.orderNumber)
        assertEquals(user, result.user)
        assertEquals(service, result.service)
        assertEquals(product, result.product)
        assertEquals(OrderStatus.PENDING, result.status)
        assertEquals(OrderType.ONE_TIME, result.type)
        assertEquals(BigDecimal("99.99"), result.totalAmount)

        verify(productRepository).findByProductIdAndServiceServiceId("product456", "service123")
        verify(userRepository).findByUserIdAndServiceServiceId("user789", "service123")
        verify(commonUtils).generateOrderNumber()
        verify(orderRepository).save(any(Order::class.java))
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
            productPrice = BigDecimal("99.99"),
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
            productPrice = BigDecimal("29.99"),
            imageUrl = "http://example.com/image.jpg",
            category = null,
            isActive = true,
            type = ProductType.SUBSCRIPTION_SERVICE,
            billingInterval = BillingInterval.MONTHLY,
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

        val orderNumber = "ORD-2024010112-000001"
        val savedOrder = Order(
            id = 1L,
            orderNumber = orderNumber,
            user = user,
            service = service,
            product = product,
            subscription = null,
            currencyCode = "USD",
            productPrice = BigDecimal("29.99"),
            totalAmount = BigDecimal("29.99"),
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

        `when`(productRepository.findByProductIdAndServiceServiceId("subscription456", "service123"))
            .thenReturn(product)
        `when`(userRepository.findByUserIdAndServiceServiceId("user789", "service123"))
            .thenReturn(Optional.of(user))
        `when`(commonUtils.generateOrderNumber()).thenReturn(orderNumber)
        `when`(orderRepository.save(any(Order::class.java))).thenReturn(savedOrder)

        // When
        val result = orderService.createOrder(request)

        // Then
        assertNotNull(result)
        assertEquals(OrderType.SUBSCRIPTION_BILLING, result.type)
        assertEquals(BigDecimal("29.99"), result.totalAmount)

        verify(orderRepository).save(any(Order::class.java))
    }
}