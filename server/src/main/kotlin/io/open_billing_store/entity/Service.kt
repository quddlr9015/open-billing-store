package io.open_billing_store.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "services")
data class Service(
    @Id
    @Column(name = "service_id", length = 10)
    val serviceId: String,

    @Column(name = "service_name", nullable = false, length = 20)
    val serviceName: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "api_key", unique = true, nullable = false)
    val apiKey: String,

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