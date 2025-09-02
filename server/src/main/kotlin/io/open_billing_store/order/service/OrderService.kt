package io.open_billing_store.order.service

import io.open_billing_store.entity.Order
import io.open_billing_store.entity.OrderStatus
import io.open_billing_store.order.request.OrderInitRequest
import io.open_billing_store.order.response.OrderInitResponse

interface OrderService {
    
    fun createOrder(orderInitRequest: OrderInitRequest): OrderInitResponse
    
    fun updateOrder(id: Long, orderDetails: Order): Order?
    
    fun confirmOrder(id: Long): Order?
    
    fun cancelOrder(id: Long): Order?
    
    fun refundOrder(id: Long): Order?
}