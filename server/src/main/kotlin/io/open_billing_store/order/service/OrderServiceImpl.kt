package io.open_billing_store.order.service

import io.open_billing_store.entity.Order
import io.open_billing_store.entity.OrderStatus
import io.open_billing_store.entity.OrderType
import io.open_billing_store.order.request.OrderInitRequest
import io.open_billing_store.order.response.OrderInitResponse
import io.open_billing_store.calculate.PriceCalculator
import io.open_billing_store.calculate.TaxCalculator
import io.open_billing_store.repository.OrderRepository
import io.open_billing_store.repository.ProductRepository
import io.open_billing_store.repository.UserRepository
import io.open_billing_store.util.CommonUtils
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.*

@Service
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val commonUtils: CommonUtils,
    private val priceCalculator: PriceCalculator,
    private val taxCalculator: TaxCalculator
) : OrderService {

    override fun createOrder(orderInitRequest: OrderInitRequest): OrderInitResponse {
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
        
        // Calculate product pricing
        val priceResult = priceCalculator.calculateProductPrice(
            orderInitRequest.productId,
            orderInitRequest.countryCode
        )
        
        // Calculate tax
        val taxResult = taxCalculator.calculateTaxAndTotalAmount(
            priceResult.finalPrice,
            orderInitRequest.countryCode
        )
        
        // Generate unique order number
        val orderNumber = commonUtils.generateOrderNumber()
        
        // Create order with calculated amounts
        val order = Order(
            orderNumber = orderNumber,
            user = user,
            service = product.service,
            product = product,
            currencyCode = priceResult.currencyCode,
            productPrice = priceResult.finalPrice,
            taxAmount = taxResult.taxAmount,
            totalAmount = taxResult.totalAmount,
            discountAmount = priceResult.discountAmount,
            status = OrderStatus.PENDING,
            type = if (product.type.name == "SUBSCRIPTION_SERVICE") OrderType.SUBSCRIPTION_BILLING else OrderType.ONE_TIME,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val savedOrder = orderRepository.save(order)

        // Format currency for display based on country
        val currencyFormatter = commonUtils.getCurrencyFormatterForCountry(
            taxResult.country.countryCode,
            priceResult.currencyCode
        )
        
        return OrderInitResponse(
            resultCode = "SUCCESS",
            orderId = savedOrder.orderNumber,
            productPrice = savedOrder.productPrice.toString(),
            displayProductPrice = currencyFormatter.format(savedOrder.productPrice),
            taxAmount = savedOrder.taxAmount.toString(),
            displayTaxAmount = currencyFormatter.format(savedOrder.taxAmount),
            totalPaymentAmount = savedOrder.totalAmount.toString(),
            displayTotalPaymentAmount = currencyFormatter.format(savedOrder.totalAmount),
            isFreeTrial = savedOrder.productPrice.compareTo(BigDecimal.ZERO) == 0,
            currencyCode = savedOrder.currencyCode
        )
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