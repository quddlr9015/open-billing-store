package io.open_billing_store.repository

import io.open_billing_store.entity.Service
import io.open_billing_store.entity.ServiceStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ServiceRepository : JpaRepository<Service, String> {
    fun findByServiceId(serviceId: String): Optional<Service>
    fun findByApiKey(apiKey: String): Optional<Service>
    fun findByStatus(status: ServiceStatus): List<Service>
    fun findByServiceNameContainingIgnoreCase(serviceName: String): List<Service>
    
    @Query("SELECT s FROM Service s WHERE s.status = 'ACTIVE'")
    fun findActiveServices(): List<Service>
    
    fun existsByServiceId(serviceId: String): Boolean
    fun existsByApiKey(apiKey: String): Boolean
}