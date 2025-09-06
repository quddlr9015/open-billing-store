package io.open_billing_store.payment.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class PaymentCreateRequest(
    @field:NotNull(message = "User ID is required")
    @field:Positive(message = "User ID must be positive")
    val userId: Long,
    
    @field:NotNull(message = "Amount is required")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    val amount: BigDecimal,
    
    @field:NotBlank(message = "Currency is required")
    val currency: String = "USD",
    
    @field:NotBlank(message = "Payment gateway is required")
    val paymentGateway: String,
    
    @field:NotBlank(message = "Payment type is required")
    val paymentType: String = "ONE_TIME",
    
    val paymentMethodId: String? = null,
    val orderId: Long? = null,
    val subscriptionId: Long? = null,
    val subscriptionPlan: SubscriptionPlanRequest? = null,
    val metadata: Map<String, String> = emptyMap()
)

data class PaymentConfirmRequest(
    @field:NotBlank(message = "Payment ID is required")
    val paymentId: String,
    
    val paymentMethodId: String? = null
)

data class PaymentRefundRequest(
    @field:NotBlank(message = "Payment ID is required")
    val paymentId: String,
    
    val amount: BigDecimal? = null,
    val reason: String? = null
)

data class SubscriptionPlanRequest(
    @field:NotBlank(message = "Interval is required")
    val interval: String, // "day", "week", "month", "year"
    
    @field:Positive(message = "Interval count must be positive")
    val intervalCount: Int = 1,
    
    val trialPeriodDays: Int? = null,
    val description: String? = null
)

data class SubscriptionCancelRequest(
    @field:NotBlank(message = "Subscription ID is required")
    val subscriptionId: String,
    
    val cancelAtPeriodEnd: Boolean = true,
    val reason: String? = null
)

data class SubscriptionUpdateRequest(
    @field:NotBlank(message = "Subscription ID is required")
    val subscriptionId: String,
    
    val amount: BigDecimal? = null,
    val paymentMethodId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)