package io.open_billing_store.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_method_tokens")
data class PaymentMethodToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "pg_provider", nullable = false, length = 50)
    val pgProvider: String,

    @Column(name = "token_id", nullable = false)
    val tokenId: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "modified_at")
    val modifiedAt: LocalDateTime = LocalDateTime.now()
)