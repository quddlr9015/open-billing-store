package io.open_billing_store.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "subscriptions")
data class Subscription(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "first_order_id")
    val firstOrderId: Long? = null,

    @Column(name = "latest_order_id")
    val latestOrderId: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "subscription_plan", nullable = false)
    val subscriptionPlan: String,

    @Enumerated(EnumType.STRING)
    val billingCycle: BillingInterval,

    @Enumerated(EnumType.STRING)
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(name = "next_billing_date")
    val nextBillingDate: LocalDate,

    @Column(name = "trial_end_date")
    val trialEndDate: LocalDate? = null,

    @Column(name = "cancelled_at")
    val cancelledAt: LocalDateTime? = null,

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    val cancelReason: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)


enum class SubscriptionStatus {
    ACTIVE, PAUSED, CANCELLED, EXPIRED, TRIAL
}