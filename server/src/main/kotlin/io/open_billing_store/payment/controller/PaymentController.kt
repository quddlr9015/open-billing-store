package io.open_billing_store.payment.controller

import io.open_billing_store.entity.PaymentStatus
import io.open_billing_store.payment.request.PaymentCreateRequest
import io.open_billing_store.payment.request.PaymentConfirmRequest
import io.open_billing_store.payment.request.PaymentRefundRequest
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

    /**
     * 새로운 결제 생성
     * 일회성 결제 또는 구독 결제를 생성합니다.
     * 
     * Create new payment
     * Creates either one-time or subscription payment.
     */
    @PostMapping("/pay")
    suspend fun createPayment(@Valid @RequestBody request: PaymentCreateRequest): ResponseEntity<PaymentResponse> {
        val response = paymentService.createPayment(request)
        val status = if (response.success) HttpStatus.CREATED else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    /**
     * 결제 승인
     * 생성된 결제를 실제로 처리하여 승인합니다.
     * 
     * Confirm payment
     * Processes and confirms the created payment.
     */
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

    /**
     * 결제 취소
     * 처리되지 않은 결제를 취소합니다. (결제 완료 전)
     * 
     * Cancel payment
     * Cancels unprocessed payment (before completion).
     */
    @PostMapping("/{paymentId}/cancel")
    suspend fun cancelPayment(@PathVariable paymentId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.cancelPayment(paymentId)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    /**
     * 결제 환불
     * 완료된 결제에 대해 전체 또는 부분 환불을 처리합니다.
     * 
     * Refund payment
     * Processes full or partial refund for completed payments.
     */
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

    /**
     * 결제 조회
     * 특정 결제 ID로 결제 정보를 조회합니다.
     * 
     * Retrieve payment
     * Retrieves payment information by specific payment ID.
     */
    @GetMapping("/{paymentId}")
    suspend fun getPayment(@PathVariable paymentId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.retrievePayment(paymentId)
        val status = if (response.success) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity.status(status).body(response)
    }

    /**
     * 사용자별 결제 내역 조회
     * 특정 사용자의 모든 결제 내역을 최신순으로 조회합니다.
     * 
     * Get payments by user
     * Retrieves all payment history for a specific user in descending order.
     */
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

    /**
     * 결제 상태별 조회
     * 특정 상태(PENDING, COMPLETED, FAILED 등)의 결제들을 조회합니다.
     * 
     * Get payments by status
     * Retrieves payments with specific status (PENDING, COMPLETED, FAILED, etc.).
     */
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

}