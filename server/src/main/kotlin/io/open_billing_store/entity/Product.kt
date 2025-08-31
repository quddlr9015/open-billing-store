package io.open_billing_store.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_id", unique = true, nullable = false, length = 50)
    val productId: String,

    @Column(nullable = false)
    val name: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(precision = 10, scale = 3, nullable = false)
    val productPrice: BigDecimal,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    val service: Service,

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