package io.open_billing_store.payment.service

import io.open_billing_store.entity.Payment
import io.open_billing_store.entity.PaymentStatus
import io.open_billing_store.payment.request.*
import io.open_billing_store.payment.response.PaymentResponse

interface PaymentService {
    suspend fun createPayment(request: PaymentCreateRequest): PaymentResponse
    suspend fun confirmPayment(request: PaymentConfirmRequest): PaymentResponse
    suspend fun cancelPayment(paymentId: String): PaymentResponse
    suspend fun refundPayment(request: PaymentRefundRequest): PaymentResponse
    suspend fun retrievePayment(paymentId: String): PaymentResponse
    
    // Subscription methods
    suspend fun cancelSubscription(request: SubscriptionCancelRequest): PaymentResponse
    suspend fun updateSubscription(request: SubscriptionUpdateRequest): PaymentResponse
    suspend fun retrieveSubscription(subscriptionId: String): PaymentResponse
    
    fun getPaymentsByUser(userId: Long): List<Payment>
    fun getPaymentsByStatus(status: PaymentStatus): List<Payment>
    fun getSubscriptionPayments(subscriptionId: Long): List<Payment>
}