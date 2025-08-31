package io.open_billing_store.util

import io.open_billing_store.entity.OrderSequence
import io.open_billing_store.repository.OrderSequenceRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class CommonUtils(
    private val orderSequenceRepository: OrderSequenceRepository
) {
    
    @Transactional
    fun generateOrderNumber(): String {
        val sequence = orderSequenceRepository.save(OrderSequence())
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        return "ORD-$timestamp-${sequence.id.toString().padStart(6, '0')}"
    }
}