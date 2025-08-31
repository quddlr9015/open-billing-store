package io.byungik.open_billing_store.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "services")
data class Service(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "service_code", unique = true, nullable = false, length = 50)
    val serviceCode: String,

    @Column(name = "service_name", nullable = false, length = 100)
    val serviceName: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "api_key", unique = true, nullable = false)
    val apiKey: String,

    @Column(name = "webhook_url")
    val webhookUrl: String? = null,

    @Column(name = "webhook_secret")
    val webhookSecret: String? = null,

    @Enumerated(EnumType.STRING)
    val status: ServiceStatus = ServiceStatus.ACTIVE,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ServiceStatus {
    ACTIVE, INACTIVE
}