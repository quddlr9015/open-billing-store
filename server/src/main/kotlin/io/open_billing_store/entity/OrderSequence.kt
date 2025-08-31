package io.open_billing_store.entity

import jakarta.persistence.*

@Entity
@Table(name = "order_sequence")
data class OrderSequence(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)