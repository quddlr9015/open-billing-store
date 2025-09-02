package io.open_billing_store.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "products",
    indexes = [Index(name = "idx_service_id", columnList = "service_id")]
)
data class Product(
    @Id
    @Column(name = "product_id", length = 10)
    val productId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    val service: Service,

    @Column(nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    val category: Category? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Enumerated(EnumType.STRING)
    val type: ProductType = ProductType.ONE_TIME_SERVICE,

    @Enumerated(EnumType.STRING)
    val billingInterval: BillingInterval? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ProductType {
    ONE_TIME_SERVICE, SUBSCRIPTION_SERVICE
}

enum class BillingInterval {
    DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY
}