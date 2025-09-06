package io.open_billing_store.payment.controller

import io.open_billing_store.entity.PaymentStatus
import io.open_billing_store.payment.request.*
import io.open_billing_store.payment.response.PaymentResponse
import io.open_billing_store.payment.service.PaymentService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @PostMapping("/pay")
    suspend fun createPayment(@Valid @RequestBody request: PaymentCreateRequest): ResponseEntity<PaymentResponse> {
        val response = paymentService.createPayment(request)
        val status = if (response.success) HttpStatus.CREATED else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    @PostMapping("/{paymentId}/confirm")
    suspend fun confirmPayment(
        @PathVariable paymentId: String,
        @RequestBody request: PaymentConfirmRequest
    ): ResponseEntity<PaymentResponse> {
        val confirmRequest = request.copy(paymentId = paymentId)
        val response = paymentService.confirmPayment(confirmRequest)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    @PostMapping("/{paymentId}/cancel")
    suspend fun cancelPayment(@PathVariable paymentId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.cancelPayment(paymentId)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    @PostMapping("/{paymentId}/refund")
    suspend fun refundPayment(
        @PathVariable paymentId: String,
        @RequestBody request: PaymentRefundRequest
    ): ResponseEntity<PaymentResponse> {
        val refundRequest = request.copy(paymentId = paymentId)
        val response = paymentService.refundPayment(refundRequest)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping("/{paymentId}")
    suspend fun getPayment(@PathVariable paymentId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.retrievePayment(paymentId)
        val status = if (response.success) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping("/user/{userId}")
    fun getPaymentsByUser(@PathVariable userId: Long): ResponseEntity<List<Any>> {
        val payments = paymentService.getPaymentsByUser(userId)
        val paymentResponses = payments.map { payment ->
            mapOf(
                "paymentId" to payment.paymentId,
                "amount" to payment.amount,
                "status" to payment.status.name,
                "paymentGateway" to payment.paymentGateway,
                "createdAt" to payment.createdAt,
                "processedAt" to payment.processedAt
            )
        }
        return ResponseEntity.ok(paymentResponses)
    }

    @GetMapping("/status/{status}")
    fun getPaymentsByStatus(@PathVariable status: String): ResponseEntity<List<Any>> {
        try {
            val paymentStatus = PaymentStatus.valueOf(status.uppercase())
            val payments = paymentService.getPaymentsByStatus(paymentStatus)
            val paymentResponses = payments.map { payment ->
                mapOf(
                    "paymentId" to payment.paymentId,
                    "userId" to payment.user.id,
                    "amount" to payment.amount,
                    "status" to payment.status.name,
                    "paymentGateway" to payment.paymentGateway,
                    "createdAt" to payment.createdAt,
                    "processedAt" to payment.processedAt
                )
            }
            return ResponseEntity.ok(paymentResponses)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(emptyList())
        }
    }

    @GetMapping("/gateways")
    fun getSupportedGateways(): ResponseEntity<List<String>> {
        val supportedGateways = listOf("STRIPE", "PAYPAL")
        return ResponseEntity.ok(supportedGateways)
    }

    @PostMapping("/subscriptions/{subscriptionId}/cancel")
    suspend fun cancelSubscription(
        @PathVariable subscriptionId: String,
        @RequestBody request: SubscriptionCancelRequest
    ): ResponseEntity<PaymentResponse> {
        val cancelRequest = request.copy(subscriptionId = subscriptionId)
        val response = paymentService.cancelSubscription(cancelRequest)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    @PutMapping("/subscriptions/{subscriptionId}")
    suspend fun updateSubscription(
        @PathVariable subscriptionId: String,
        @RequestBody request: SubscriptionUpdateRequest
    ): ResponseEntity<PaymentResponse> {
        val updateRequest = request.copy(subscriptionId = subscriptionId)
        val response = paymentService.updateSubscription(updateRequest)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping("/subscriptions/{subscriptionId}")
    suspend fun getSubscription(@PathVariable subscriptionId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.retrieveSubscription(subscriptionId)
        val status = if (response.success) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping("/subscriptions/{subscriptionId}/payments")
    fun getSubscriptionPayments(@PathVariable subscriptionId: Long): ResponseEntity<List<Any>> {
        val payments = paymentService.getSubscriptionPayments(subscriptionId)
        val paymentResponses = payments.map { payment ->
            mapOf(
                "paymentId" to payment.paymentId,
                "amount" to payment.amount,
                "status" to payment.status.name,
                "type" to payment.type.name,
                "paymentGateway" to payment.paymentGateway,
                "createdAt" to payment.createdAt,
                "processedAt" to payment.processedAt
            )
        }
        return ResponseEntity.ok(paymentResponses)
    }

    @GetMapping("/types")
    fun getSupportedPaymentTypes(): ResponseEntity<List<String>> {
        val supportedTypes = listOf("ONE_TIME", "RECURRING")
        return ResponseEntity.ok(supportedTypes)
    }
}