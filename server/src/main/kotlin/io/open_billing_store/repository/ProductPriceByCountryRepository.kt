package io.open_billing_store.repository

import io.open_billing_store.entity.Product
import io.open_billing_store.entity.ProductPriceByCountry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ProductPriceByCountryRepository : JpaRepository<ProductPriceByCountry, Long> {
    
    fun findByProductAndCountryCodeAndIsActiveTrue(product: Product, countryCode: String): ProductPriceByCountry?
    
    fun findByProduct_ProductIdAndCountryCodeAndIsActiveTrue(productId: String, countryCode: String): ProductPriceByCountry?
    
    fun findByCountryCodeAndIsActiveTrue(countryCode: String): List<ProductPriceByCountry>
    
    fun findByProductAndIsActiveTrue(product: Product): List<ProductPriceByCountry>
    
    @Query("""
        SELECT ppc FROM ProductPriceByCountry ppc 
        WHERE ppc.product.productId = :productId 
        AND ppc.countryCode = :countryCode 
        AND ppc.isActive = true
        AND ppc.effectiveFrom <= :currentDateTime
        AND (ppc.effectiveTo IS NULL OR ppc.effectiveTo > :currentDateTime)
        ORDER BY ppc.effectiveFrom DESC
    """)
    fun findActiveProductPriceByProductIdAndCountryCode(
        productId: String, 
        countryCode: String, 
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): ProductPriceByCountry?
    
    @Query("""
        SELECT ppc FROM ProductPriceByCountry ppc 
        WHERE ppc.product = :product 
        AND ppc.countryCode = :countryCode 
        AND ppc.isActive = true
        AND ppc.effectiveFrom <= :currentDateTime
        AND (ppc.effectiveTo IS NULL OR ppc.effectiveTo > :currentDateTime)
        ORDER BY ppc.effectiveFrom DESC
    """)
    fun findActiveProductPriceByProductAndCountryCode(
        product: Product, 
        countryCode: String, 
        currentDateTime: LocalDateTime = LocalDateTime.now()
    ): ProductPriceByCountry?
}