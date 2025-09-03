package io.open_billing_store.repository

import io.open_billing_store.entity.Product
import io.open_billing_store.entity.ProductType
import io.open_billing_store.entity.Service
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, String> {
    fun findByIsActiveTrue(): List<Product>
    fun findByCategoryIdAndIsActiveTrue(categoryId: Long): List<Product>
    fun findByTypeAndIsActiveTrue(type: ProductType): List<Product>
    
    // Service-based queries
    fun findByServiceServiceId(serviceId: String): List<Product>
    fun findByService(service: Service): List<Product>
    fun findByServiceServiceIdAndIsActiveTrue(serviceId: String): List<Product>
    fun findByServiceAndIsActiveTrue(service: Service): List<Product>
    fun findByServiceServiceIdAndTypeAndIsActiveTrue(serviceId: String, type: ProductType): List<Product>
    
    // Find by productId and serviceId
    fun findByProductIdAndServiceServiceId(productId: String, serviceId: String): Product?
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isActive = true")
    fun findByNameContainingAndIsActiveTrue(name: String): List<Product>
    
    @Query("SELECT p FROM Product p WHERE p.service.serviceId = :serviceId AND p.name LIKE %:name% AND p.isActive = true")
    fun findByServiceServiceIdAndNameContainingAndIsActiveTrue(serviceId: String, name: String): List<Product>
}