package io.open_billing_store.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
data class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "payment_id", unique = true, nullable = false)
    val paymentId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    val order: Order? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    val subscription: Subscription? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(precision = 10, scale = 2, nullable = false)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    val method: PaymentMethod,

    @Enumerated(EnumType.STRING)
    val status: PaymentStatus = PaymentStatus.PENDING,

    @Enumerated(EnumType.STRING)
    val type: PaymentType = PaymentType.ONE_TIME,

    @Column(name = "external_transaction_id")
    val externalTransactionId: String? = null,

    @Column(name = "external_subscription_id")
    val externalSubscriptionId: String? = null,

    @Column(name = "payment_gateway")
    val paymentGateway: String? = null,

    @Column(name = "failure_reason")
    val failureReason: String? = null,

    @Column(name = "processed_at")
    val processedAt: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentMethod {
    STRIPE,
    PAYPAL
}

enum class PaymentType {
    ONE_TIME, RECURRING
}

enum class PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REFUNDED
}