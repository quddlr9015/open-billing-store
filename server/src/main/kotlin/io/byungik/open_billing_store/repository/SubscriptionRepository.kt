package io.byungik.open_billing_store.repository

import io.byungik.open_billing_store.entity.Subscription
import io.byungik.open_billing_store.entity.SubscriptionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByUserIdAndStatus(userId: Long, status: SubscriptionStatus): List<Subscription>
    fun findByStatus(status: SubscriptionStatus): List<Subscription>
    fun findByNextBillingDateBeforeAndStatus(date: LocalDate, status: SubscriptionStatus): List<Subscription>
    
    @Query("SELECT s FROM Subscription s WHERE s.nextBillingDate <= :date AND s.status = 'ACTIVE'")
    fun findSubscriptionsDueForBilling(date: LocalDate): List<Subscription>
}