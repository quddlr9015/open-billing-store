package io.open_billing_store.payment.service

import io.open_billing_store.entity.*
import io.open_billing_store.payment.gateway.PaymentGateway
import io.open_billing_store.payment.gateway.PaymentGatewayResponse
import io.open_billing_store.payment.request.PaymentCreateRequest
import io.open_billing_store.payment.request.PaymentConfirmRequest
import io.open_billing_store.payment.request.PaymentRefundRequest
import io.open_billing_store.repository.OrderRepository
import io.open_billing_store.repository.PaymentRepository
import io.open_billing_store.repository.UserRepository
import io.open_billing_store.repository.SubscriptionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PaymentServiceImplTest {

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var orderRepository: OrderRepository

    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Mock
    private lateinit var stripeGateway: PaymentGateway

    private lateinit var paymentService: PaymentServiceImpl

    private val testService = Service(
        serviceId = "TEST001",
        serviceName = "Test Service",
        apiKey = "test-api-key"
    )

    private val testUser = User(
        id = 1L,
        userId = "user123",
        service = testService,
        email = "test@example.com",
        firstName = "Test",
        lastName = "User"
    )

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(stripeGateway.getProviderName()).thenReturn("STRIPE")
        paymentService = PaymentServiceImpl(
            paymentRepository,
            userRepository,
            orderRepository,
            subscriptionRepository,
            listOf(stripeGateway)
        )
    }

    @Test
    fun `createPayment should create payment successfully`() = runTest {
        val request = PaymentCreateRequest(
            userId = 1L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            paymentGateway = "STRIPE"
        )

        val gatewayResponse = PaymentGatewayResponse(
            success = true,
            paymentId = "pi_test123",
            externalTransactionId = "pi_test123",
            status = "requires_confirmation",
            amount = BigDecimal("100.00"),
            currency = "USD"
        )

        val payment = Payment(
            id = 1L,
            paymentId = "pi_test123",
            user = testUser,
            amount = BigDecimal("100.00"),
            method = PaymentMethod.STRIPE,
            status = PaymentStatus.PENDING,
            externalTransactionId = "pi_test123",
            paymentGateway = "STRIPE"
        )

        whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser))
        whenever(stripeGateway.createPayment(any())).thenReturn(gatewayResponse)
        whenever(paymentRepository.save(any<Payment>())).thenReturn(payment)

        val response = paymentService.createPayment(request)

        assertTrue(response.success)
        assertEquals("pi_test123", response.paymentId)
        assertEquals("PENDING", response.status)
        assertEquals(BigDecimal("100.00"), response.amount)
    }

    @Test
    fun `createPayment should handle gateway failure`() = runTest {
        val request = PaymentCreateRequest(
            userId = 1L,
            amount = BigDecimal("100.00"),
            currency = "USD",
            paymentGateway = "STRIPE"
        )

        val gatewayResponse = PaymentGatewayResponse(
            success = false,
            status = "failed",
            errorMessage = "Card declined",
            errorCode = "CARD_DECLINED"
        )

        whenever(userRepository.findById(1L)).thenReturn(Optional.of(testUser))
        whenever(stripeGateway.createPayment(any())).thenReturn(gatewayResponse)

        val response = paymentService.createPayment(request)

        assertFalse(response.success)
        assertEquals("FAILED", response.status)
        assertEquals("Card declined", response.errorMessage)
        assertEquals("CARD_DECLINED", response.errorCode)
    }

    @Test
    fun `confirmPayment should confirm payment successfully`() = runTest {
        val payment = Payment(
            id = 1L,
            paymentId = "pi_test123",
            user = testUser,
            amount = BigDecimal("100.00"),
            method = PaymentMethod.STRIPE,
            status = PaymentStatus.PENDING,
            externalTransactionId = "pi_test123",
            paymentGateway = "STRIPE"
        )

        val confirmedPayment = payment.copy(
            status = PaymentStatus.COMPLETED,
            processedAt = LocalDateTime.now()
        )

        val gatewayResponse = PaymentGatewayResponse(
            success = true,
            paymentId = "pi_test123",
            status = "succeeded",
            amount = BigDecimal("100.00"),
            currency = "USD"
        )

        val request = PaymentConfirmRequest(paymentId = "pi_test123")

        whenever(paymentRepository.findByPaymentId("pi_test123")).thenReturn(Optional.of(payment))
        whenever(stripeGateway.confirmPayment("pi_test123")).thenReturn(gatewayResponse)
        whenever(paymentRepository.save(any<Payment>())).thenReturn(confirmedPayment)

        val response = paymentService.confirmPayment(request)

        assertTrue(response.success)
        assertEquals("COMPLETED", response.status)
    }

    @Test
    fun `refundPayment should process refund successfully`() = runTest {
        val payment = Payment(
            id = 1L,
            paymentId = "pi_test123",
            user = testUser,
            amount = BigDecimal("100.00"),
            method = PaymentMethod.STRIPE,
            status = PaymentStatus.COMPLETED,
            externalTransactionId = "pi_test123",
            paymentGateway = "STRIPE"
        )

        val gatewayResponse = PaymentGatewayResponse(
            success = true,
            paymentId = "re_test123",
            status = "refunded",
            amount = BigDecimal("100.00")
        )

        val request = PaymentRefundRequest(paymentId = "pi_test123")

        whenever(paymentRepository.findByPaymentId("pi_test123")).thenReturn(Optional.of(payment))
        whenever(stripeGateway.refundPayment("pi_test123", null)).thenReturn(gatewayResponse)
        whenever(paymentRepository.save(any<Payment>())).thenReturn(payment.copy(status = PaymentStatus.REFUNDED))

        val response = paymentService.refundPayment(request)

        assertTrue(response.success)
        assertEquals("REFUNDED", response.status)
    }

    @Test
    fun `retrievePayment should return payment details`() = runTest {
        val payment = Payment(
            id = 1L,
            paymentId = "pi_test123",
            user = testUser,
            amount = BigDecimal("100.00"),
            method = PaymentMethod.STRIPE,
            status = PaymentStatus.COMPLETED,
            externalTransactionId = "pi_test123",
            paymentGateway = "STRIPE",
            createdAt = LocalDateTime.now()
        )

        whenever(paymentRepository.findByPaymentId("pi_test123")).thenReturn(Optional.of(payment))

        val response = paymentService.retrievePayment("pi_test123")

        assertTrue(response.success)
        assertEquals("pi_test123", response.paymentId)
        assertEquals("COMPLETED", response.status)
        assertEquals(BigDecimal("100.00"), response.amount)
    }
}