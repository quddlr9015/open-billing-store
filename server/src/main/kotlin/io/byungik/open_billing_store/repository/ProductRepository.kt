package io.byungik.open_billing_store.repository

import io.byungik.open_billing_store.entity.Product
import io.byungik.open_billing_store.entity.ProductType
import io.byungik.open_billing_store.entity.Service
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    fun findByIsActiveTrue(): List<Product>
    fun findByCategoryIdAndIsActiveTrue(categoryId: Long): List<Product>
    fun findByTypeAndIsActiveTrue(type: ProductType): List<Product>
    
    // Service-based queries
    fun findByServiceId(serviceId: Long): List<Product>
    fun findByService(service: Service): List<Product>
    fun findByServiceIdAndIsActiveTrue(serviceId: Long): List<Product>
    fun findByServiceAndIsActiveTrue(service: Service): List<Product>
    fun findByServiceIdAndTypeAndIsActiveTrue(serviceId: Long, type: ProductType): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isActive = true")
    fun findByNameContainingAndIsActiveTrue(name: String): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.service.id = :serviceId AND p.name LIKE %:name% AND p.isActive = true")
    fun findByServiceIdAndNameContainingAndIsActiveTrue(serviceId: Long, name: String): List<Product>
}