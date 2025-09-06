package io.open_billing_store.payment.response

import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentResponse(
    val success: Boolean,
    val paymentId: String?,
    val externalTransactionId: String?,
    val externalSubscriptionId: String? = null,
    val status: String,
    val paymentType: String?,
    val amount: BigDecimal?,
    val currency: String?,
    val paymentGateway: String?,
    val subscriptionDetails: SubscriptionDetails? = null,
    val errorMessage: String? = null,
    val errorCode: String? = null,
    val createdAt: LocalDateTime? = null,
    val processedAt: LocalDateTime? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class SubscriptionDetails(
    val subscriptionId: String,
    val status: String,
    val currentPeriodStart: LocalDateTime?,
    val currentPeriodEnd: LocalDateTime?,
    val interval: String?,
    val intervalCount: Int?,
    val trialEnd: LocalDateTime? = null,
    val cancelAtPeriodEnd: Boolean = false
)