package io.open_billing_store.order.controller

import io.open_billing_store.entity.Order
import io.open_billing_store.order.request.OrderInitRequest
import io.open_billing_store.order.service.OrderService
import io.open_billing_store.repository.OrderRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping("/init")
    fun orderInit(
        @RequestBody request: OrderInitRequest,
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<Order> {
        val order = orderService.createOrder(request);
        return ResponseEntity.ok(order)
    }
}