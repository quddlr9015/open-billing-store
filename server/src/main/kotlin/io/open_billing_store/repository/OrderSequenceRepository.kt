package io.open_billing_store.repository

import io.open_billing_store.entity.OrderSequence
import org.springframework.data.jpa.repository.JpaRepository

interface OrderSequenceRepository : JpaRepository<OrderSequence, Long>