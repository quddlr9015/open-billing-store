package io.open_billing_store.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "countries",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_country_code", columnNames = ["country_code"])
    ],
    indexes = [
        Index(name = "idx_country_code", columnList = "country_code")
    ]
)
data class Country(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "country_code", unique = true, nullable = false, length = 2)
    val countryCode: String,

    @Column(name = "country_name", nullable = false, length = 100)
    val countryName: String,

    @Column(name = "state_code", length = 3)
    val stateCode: String? = null,

    @Column(name = "state_name", length = 100)
    val stateName: String? = null,

    @Column(name = "currency_code", nullable = false, length = 3)
    val currencyCode: String,

    @Column(name = "tax_rate", precision = 5, scale = 4, nullable = false)
    val taxRate: BigDecimal,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)