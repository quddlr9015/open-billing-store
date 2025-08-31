package io.open_billing_store.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
data class Order(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "order_number", unique = true, nullable = false)
    val orderNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    val service: Service,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    val subscription: Subscription? = null,

    @Column(name = "currency_code", length = 3, nullable = false)
    val currencyCode: String,

    @Column(name = "product_price", precision = 10, scale = 2, nullable = false)
    val productPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_amount", precision = 10, scale = 2)
    val taxAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "coupon_number", length = 50)
    val couponNumber: String? = null,

    @Column(name = "coupon_amount", precision = 10, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    val status: OrderStatus = OrderStatus.PENDING,

    @Enumerated(EnumType.STRING)
    val type: OrderType,

    @Column(name = "due_date")
    val dueDate: LocalDate? = null,

    @Column(name = "paid_at")
    val paidAt: LocalDateTime? = null,

    @Column(name = "billing_address", columnDefinition = "TEXT")
    val billingAddress: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderStatus {
    PENDING, CONFIRMED, PAID, CANCELLED, REFUNDED
}

enum class OrderType {
    ONE_TIME, SUBSCRIPTION_BILLING
}