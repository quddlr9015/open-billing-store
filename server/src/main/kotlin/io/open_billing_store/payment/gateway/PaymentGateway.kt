package io.open_billing_store.payment.gateway

import java.math.BigDecimal
import java.time.LocalDateTime

interface PaymentGateway {
    fun getProviderName(): String
    
    // One-time payment methods
    suspend fun createPayment(request: PaymentRequest): PaymentGatewayResponse
    suspend fun confirmPayment(paymentIntentId: String): PaymentGatewayResponse
    suspend fun cancelPayment(paymentIntentId: String): PaymentGatewayResponse
    suspend fun refundPayment(paymentId: String, amount: BigDecimal? = null): PaymentGatewayResponse
    suspend fun retrievePayment(paymentId: String): PaymentGatewayResponse
    
    // Subscription methods
    suspend fun createSubscription(request: SubscriptionRequest): PaymentGatewayResponse
    suspend fun cancelSubscription(subscriptionId: String, cancelAtPeriodEnd: Boolean = true): PaymentGatewayResponse
    suspend fun updateSubscription(subscriptionId: String, request: SubscriptionUpdateRequest): PaymentGatewayResponse
    suspend fun retrieveSubscription(subscriptionId: String): PaymentGatewayResponse
}

data class PaymentRequest(
    val amount: BigDecimal,
    val currency: String = "USD",
    val paymentMethodId: String? = null,
    val customerId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class SubscriptionRequest(
    val customerId: String,
    val paymentMethodId: String?,
    val amount: BigDecimal,
    val currency: String = "USD",
    val interval: String, // "day", "week", "month", "year"
    val intervalCount: Int = 1,
    val trialPeriodDays: Int? = null,
    val description: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class SubscriptionUpdateRequest(
    val amount: BigDecimal? = null,
    val paymentMethodId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class PaymentGatewayResponse(
    val success: Boolean,
    val paymentId: String? = null,
    val externalTransactionId: String? = null,
    val externalSubscriptionId: String? = null,
    val status: String,
    val amount: BigDecimal? = null,
    val currency: String? = null,
    val subscriptionDetails: GatewaySubscriptionDetails? = null,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class GatewaySubscriptionDetails(
    val subscriptionId: String,
    val status: String,
    val currentPeriodStart: LocalDateTime?,
    val currentPeriodEnd: LocalDateTime?,
    val interval: String?,
    val intervalCount: Int?,
    val trialEnd: LocalDateTime? = null,
    val cancelAtPeriodEnd: Boolean = false
)