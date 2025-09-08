package io.open_billing_store.subscription.controller

import io.open_billing_store.payment.request.SubscriptionCancelRequest
import io.open_billing_store.payment.request.SubscriptionUpdateRequest
import io.open_billing_store.payment.response.PaymentResponse
import io.open_billing_store.payment.service.PaymentService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val paymentService: PaymentService
) {

    /**
     * 구독 취소
     * 활성화된 구독을 취소합니다. 기간 종료 시점에 취소하거나 즉시 취소할 수 있습니다.
     * 
     * Cancel subscription
     * Cancels an active subscription. Can be canceled at period end or immediately.
     */
    @PostMapping("/{subscriptionId}/cancel")
    suspend fun cancelSubscription(
        @PathVariable subscriptionId: String,
        @RequestBody request: SubscriptionCancelRequest
    ): ResponseEntity<PaymentResponse> {
        val cancelRequest = request.copy(subscriptionId = subscriptionId)
        val response = paymentService.cancelSubscription(cancelRequest)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    /**
     * 구독 업데이트
     * 구독의 결제 방법, 금액 등을 업데이트합니다.
     * 
     * Update subscription
     * Updates subscription payment method, amount, etc.
     */
    @PutMapping("/{subscriptionId}")
    suspend fun updateSubscription(
        @PathVariable subscriptionId: String,
        @RequestBody request: SubscriptionUpdateRequest
    ): ResponseEntity<PaymentResponse> {
        val updateRequest = request.copy(subscriptionId = subscriptionId)
        val response = paymentService.updateSubscription(updateRequest)
        val status = if (response.success) HttpStatus.OK else HttpStatus.BAD_REQUEST
        return ResponseEntity.status(status).body(response)
    }

    /**
     * 구독 조회
     * 특정 구독 ID로 구독 정보를 조회합니다.
     * 
     * Retrieve subscription
     * Retrieves subscription information by specific subscription ID.
     */
    @GetMapping("/{subscriptionId}")
    suspend fun getSubscription(@PathVariable subscriptionId: String): ResponseEntity<PaymentResponse> {
        val response = paymentService.retrieveSubscription(subscriptionId)
        val status = if (response.success) HttpStatus.OK else HttpStatus.NOT_FOUND
        return ResponseEntity.status(status).body(response)
    }

    /**
     * 구독 결제 내역 조회
     * 특정 구독과 관련된 모든 결제 내역을 조회합니다.
     * 
     * Get subscription payments
     * Retrieves all payment history related to a specific subscription.
     */
    @GetMapping("/{subscriptionId}/payments")
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
}