package io.byungik.open_billing_store.entity

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "subscription_plan", nullable = false)
    val subscriptionPlan: String,

    @Enumerated(EnumType.STRING)
    val interval: BillingInterval,

    @Column(name = "price_per_cycle", precision = 10, scale = 2, nullable = false)
    val pricePerCycle: BigDecimal,

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

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class BillingInterval {
    DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
}

enum class SubscriptionStatus {
    ACTIVE, PAUSED, CANCELLED, EXPIRED, TRIAL
}