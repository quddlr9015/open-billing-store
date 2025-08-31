package io.byungik.open_billing_store.repository

import io.byungik.open_billing_store.entity.Payment
import io.byungik.open_billing_store.entity.PaymentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByPaymentId(paymentId: String): Optional<Payment>
    fun findByOrderId(orderId: Long): List<Payment>
    fun findByUserIdOrderByCreatedAtDesc(userId: Long): List<Payment>
    fun findByStatus(status: PaymentStatus): List<Payment>
    fun findByExternalTransactionId(externalTransactionId: String): Optional<Payment>
}