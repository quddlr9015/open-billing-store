package io.open_billing_store.repository

import io.open_billing_store.entity.Country
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CountryRepository : JpaRepository<Country, Long> {
    
    fun findByCountryCodeAndIsActiveTrue(countryCode: String): Country?
    
    fun findByCountryCodeAndStateCodeAndIsActiveTrue(countryCode: String, stateCode: String?): Country?
    
    fun findByIsActiveTrue(): List<Country>
    
    fun findByCountryCodeIgnoreCaseAndIsActiveTrue(countryCode: String): Country?
}