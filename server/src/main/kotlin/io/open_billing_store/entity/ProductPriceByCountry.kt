package io.open_billing_store.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "product_price_by_country",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_product_country", columnNames = ["product_id", "country_code"])
    ],
    indexes = [
        Index(name = "idx_product_id", columnList = "product_id"),
        Index(name = "idx_country_code", columnList = "country_code"),
        Index(name = "idx_product_country", columnList = "product_id, country_code")
    ]
)
data class ProductPriceByCountry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "country_code", nullable = false, length = 2)
    val countryCode: String,

    @Column(name = "country_name", nullable = false, length = 100)
    val countryName: String,

    @Column(name = "price", precision = 10, scale = 3, nullable = false)
    val price: BigDecimal,

    @Column(name = "currency_code", nullable = false, length = 3)
    val currencyCode: String,

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    val discountPercentage: BigDecimal? = null,

    @Column(name = "discounted_price", precision = 10, scale = 3)
    val discountedPrice: BigDecimal? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "effective_from")
    val effectiveFrom: LocalDateTime = LocalDateTime.now(),

    @Column(name = "effective_to")
    val effectiveTo: LocalDateTime? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)