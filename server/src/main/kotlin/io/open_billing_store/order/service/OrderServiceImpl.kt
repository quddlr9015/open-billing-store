package io.open_billing_store.order.service

import io.open_billing_store.entity.Order
import io.open_billing_store.entity.OrderStatus
import io.open_billing_store.entity.OrderType
import io.open_billing_store.order.request.OrderInitRequest
import io.open_billing_store.repository.OrderRepository
import io.open_billing_store.repository.ProductRepository
import io.open_billing_store.repository.UserRepository
import io.open_billing_store.util.CommonUtils
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val commonUtils: CommonUtils
) : OrderService {

    override fun createOrder(orderInitRequest: OrderInitRequest): Order {
        // Fetch product with service information
        val product = productRepository.findByProductIdAndServiceServiceId(
            orderInitRequest.productId, 
            orderInitRequest.serviceId
        ) ?: throw RuntimeException("Product not found with id: ${orderInitRequest.productId} and service: ${orderInitRequest.serviceId}")
        
        // Fetch user by userId and serviceId
        val user = userRepository.findByUserIdAndServiceServiceId(
            orderInitRequest.userId, 
            orderInitRequest.serviceId
        ).orElseThrow { throw RuntimeException("User not found with userId: ${orderInitRequest.userId} and serviceId: ${orderInitRequest.serviceId}") }
        
        // Generate unique order number
        val orderNumber = commonUtils.generateOrderNumber()
        
        // Create order with product data
        val order = Order(
            orderNumber = orderNumber,
            user = user,
            service = product.service,
            product = product,
            currencyCode = "USD", //TODO: create country table and productPriceByCountry table
            productPrice = product.productPrice,
            totalAmount = product.productPrice, // Will be calculated with taxes/discounts later
            status = OrderStatus.PENDING,
            type = if (product.type.name == "SUBSCRIPTION_SERVICE") OrderType.SUBSCRIPTION_BILLING else OrderType.ONE_TIME,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        return orderRepository.save(order)
    }

    override fun updateOrder(id: Long, orderDetails: Order): Order? {
        TODO("Not yet implemented")
    }

    override fun confirmOrder(id: Long): Order? {
        TODO("Not yet implemented")
    }

    override fun cancelOrder(id: Long): Order? {
        TODO("Not yet implemented")
    }

    override fun refundOrder(id: Long): Order? {
        TODO("Not yet implemented")
    }
}