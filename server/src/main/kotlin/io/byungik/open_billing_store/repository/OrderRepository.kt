package io.byungik.open_billing_store.repository

import io.byungik.open_billing_store.entity.Order
import io.byungik.open_billing_store.entity.OrderStatus
import io.byungik.open_billing_store.entity.OrderType
import io.byungik.open_billing_store.entity.Service
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface OrderRepository : JpaRepository<Order, Long> {
    fun findByOrderNumber(orderNumber: String): Optional<Order>
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>
    fun findByStatus(status: OrderStatus): List<Order>
    fun findByType(type: OrderType): List<Order>
    fun findByUserIdAndStatus(userId: Long, status: OrderStatus): List<Order>
    fun findByDueDateBeforeAndStatus(dueDate: LocalDate, status: OrderStatus): List<Order>
    
    // Service-based queries
    fun findByServiceId(serviceId: Long): List<Order>
    fun findByService(service: Service): List<Order>
    fun findByServiceIdAndStatus(serviceId: Long, status: OrderStatus): List<Order>
    fun findByServiceAndUserIdOrderByCreatedAtDesc(service: Service, userId: Long): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.dueDate < :date AND o.status = 'CONFIRMED'")
    fun findOverdueOrders(date: LocalDate): List<Order>
    
    @Query("SELECT o FROM Order o WHERE o.service.id = :serviceId AND o.dueDate < :date AND o.status = 'CONFIRMED'")
    fun findOverdueOrdersByService(serviceId: Long, date: LocalDate): List<Order>
}