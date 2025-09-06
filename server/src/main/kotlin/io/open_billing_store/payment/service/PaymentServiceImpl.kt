package io.open_billing_store.payment.service

import io.open_billing_store.entity.*
import io.open_billing_store.payment.gateway.*
import io.open_billing_store.payment.request.*
import io.open_billing_store.payment.response.*
import io.open_billing_store.payment.gateway.SubscriptionUpdateRequest as GatewaySubscriptionUpdateRequest
import io.open_billing_store.repository.PaymentRepository
import io.open_billing_store.repository.UserRepository
import io.open_billing_store.repository.OrderRepository
import io.open_billing_store.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentGateways: List<PaymentGateway>
) : PaymentService {

    private fun getPaymentGateway(gatewayName: String): PaymentGateway {
        return paymentGateways.find { it.getProviderName().equals(gatewayName, ignoreCase = true) }
            ?: throw IllegalArgumentException("Payment gateway not found: $gatewayName")
    }

    override suspend fun createPayment(request: PaymentCreateRequest): PaymentResponse {
        try {
            val user = userRepository.findById(request.userId)
                .orElseThrow { IllegalArgumentException("User not found: ${request.userId}") }

            val order = request.orderId?.let { orderId ->
                orderRepository.findById(orderId)
                    .orElseThrow { IllegalArgumentException("Order not found: $orderId") }
            }

            val subscription = request.subscriptionId?.let { subscriptionId ->
                subscriptionRepository.findById(subscriptionId)
                    .orElseThrow { IllegalArgumentException("Subscription not found: $subscriptionId") }
            }

            val gateway = getPaymentGateway(request.paymentGateway)
            val paymentType = PaymentType.valueOf(request.paymentType.uppercase())
            
            val gatewayResponse = if (paymentType == PaymentType.RECURRING && request.subscriptionPlan != null) {
                val subscriptionRequest = SubscriptionRequest(
                    customerId = user.id.toString(),
                    paymentMethodId = request.paymentMethodId,
                    amount = request.amount,
                    currency = request.currency,
                    interval = request.subscriptionPlan.interval,
                    intervalCount = request.subscriptionPlan.intervalCount,
                    trialPeriodDays = request.subscriptionPlan.trialPeriodDays,
                    description = request.subscriptionPlan.description,
                    metadata = request.metadata
                )
                gateway.createSubscription(subscriptionRequest)
            } else {
                val paymentRequest = PaymentRequest(
                    amount = request.amount,
                    currency = request.currency,
                    paymentMethodId = request.paymentMethodId,
                    customerId = user.id.toString(),
                    metadata = request.metadata
                )
                gateway.createPayment(paymentRequest)
            }

            if (gatewayResponse.success) {
                val payment = Payment(
                    paymentId = gatewayResponse.paymentId ?: UUID.randomUUID().toString(),
                    order = order,
                    subscription = subscription,
                    user = user,
                    amount = request.amount,
                    method = PaymentMethod.valueOf(request.paymentGateway.uppercase()),
                    status = mapGatewayStatusToPaymentStatus(gatewayResponse.status),
                    type = paymentType,
                    externalTransactionId = gatewayResponse.externalTransactionId,
                    externalSubscriptionId = gatewayResponse.externalSubscriptionId,
                    paymentGateway = request.paymentGateway.uppercase(),
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                val savedPayment = paymentRepository.save(payment)

                return PaymentResponse(
                    success = true,
                    paymentId = savedPayment.paymentId,
                    externalTransactionId = savedPayment.externalTransactionId,
                    externalSubscriptionId = savedPayment.externalSubscriptionId,
                    status = savedPayment.status.name,
                    paymentType = savedPayment.type.name,
                    amount = savedPayment.amount,
                    currency = request.currency,
                    paymentGateway = savedPayment.paymentGateway,
                    subscriptionDetails = gatewayResponse.subscriptionDetails?.let { details ->
                        SubscriptionDetails(
                            subscriptionId = details.subscriptionId,
                            status = details.status,
                            currentPeriodStart = details.currentPeriodStart,
                            currentPeriodEnd = details.currentPeriodEnd,
                            interval = details.interval,
                            intervalCount = details.intervalCount,
                            trialEnd = details.trialEnd,
                            cancelAtPeriodEnd = details.cancelAtPeriodEnd
                        )
                    },
                    createdAt = savedPayment.createdAt,
                    metadata = gatewayResponse.metadata
                )
            } else {
                return PaymentResponse(
                    success = false,
                    paymentId = null,
                    externalTransactionId = null,
                    status = "FAILED",
                    paymentType = request.paymentType,
                    amount = request.amount,
                    currency = request.currency,
                    paymentGateway = request.paymentGateway,
                    errorMessage = gatewayResponse.errorMessage,
                    errorCode = gatewayResponse.errorCode
                )
            }
        } catch (e: Exception) {
            return PaymentResponse(
                success = false,
                paymentId = null,
                externalTransactionId = null,
                status = "FAILED",
                paymentType = request.paymentType,
                amount = request.amount,
                currency = request.currency,
                paymentGateway = request.paymentGateway,
                errorMessage = e.message,
                errorCode = "INTERNAL_ERROR"
            )
        }
    }

    override suspend fun confirmPayment(request: PaymentConfirmRequest): PaymentResponse {
        return try {
            val payment = paymentRepository.findByPaymentId(request.paymentId)
                .orElseThrow { IllegalArgumentException("Payment not found: ${request.paymentId}") }

            val gateway = getPaymentGateway(payment.paymentGateway ?: "STRIPE")
            val gatewayResponse = gateway.confirmPayment(payment.externalTransactionId ?: payment.paymentId)

            if (gatewayResponse.success) {
                val updatedPayment = payment.copy(
                    status = mapGatewayStatusToPaymentStatus(gatewayResponse.status),
                    processedAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
                paymentRepository.save(updatedPayment)

                PaymentResponse(
                    success = true,
                    paymentId = updatedPayment.paymentId,
                    externalTransactionId = updatedPayment.externalTransactionId,
                    status = updatedPayment.status.name,
                    paymentType = updatedPayment.type.name,
                    amount = updatedPayment.amount,
                    currency = "USD",
                    paymentGateway = updatedPayment.paymentGateway,
                    processedAt = updatedPayment.processedAt,
                    metadata = gatewayResponse.metadata
                )
            } else {
                PaymentResponse(
                    success = false,
                    paymentId = payment.paymentId,
                    externalTransactionId = payment.externalTransactionId,
                    status = "FAILED",
                    paymentType = payment.type.name,
                    amount = payment.amount,
                    currency = "USD",
                    paymentGateway = payment.paymentGateway,
                    errorMessage = gatewayResponse.errorMessage,
                    errorCode = gatewayResponse.errorCode
                )
            }
        } catch (e: Exception) {
            PaymentResponse(
                success = false,
                paymentId = request.paymentId,
                externalTransactionId = null,
                status = "FAILED",
                paymentType = "ONE_TIME",
                amount = null,
                currency = null,
                paymentGateway = null,
                errorMessage = e.message,
                errorCode = "INTERNAL_ERROR"
            )
        }
    }

    override suspend fun cancelPayment(paymentId: String): PaymentResponse {
        return try {
            val payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow { IllegalArgumentException("Payment not found: $paymentId") }

            val gateway = getPaymentGateway(payment.paymentGateway ?: "STRIPE")
            val gatewayResponse = gateway.cancelPayment(payment.externalTransactionId ?: payment.paymentId)

            val updatedPayment = payment.copy(
                status = PaymentStatus.CANCELLED,
                updatedAt = LocalDateTime.now()
            )
            paymentRepository.save(updatedPayment)

            PaymentResponse(
                success = gatewayResponse.success,
                paymentId = updatedPayment.paymentId,
                externalTransactionId = updatedPayment.externalTransactionId,
                status = updatedPayment.status.name,
                paymentType = updatedPayment.type.name,
                amount = updatedPayment.amount,
                currency = "USD",
                paymentGateway = updatedPayment.paymentGateway,
                errorMessage = gatewayResponse.errorMessage,
                errorCode = gatewayResponse.errorCode
            )
        } catch (e: Exception) {
            PaymentResponse(
                success = false,
                paymentId = paymentId,
                externalTransactionId = null,
                status = "FAILED",
                paymentType = "ONE_TIME",
                amount = null,
                currency = null,
                paymentGateway = null,
                errorMessage = e.message,
                errorCode = "INTERNAL_ERROR"
            )
        }
    }

    override suspend fun refundPayment(request: PaymentRefundRequest): PaymentResponse {
        return try {
            val payment = paymentRepository.findByPaymentId(request.paymentId)
                .orElseThrow { IllegalArgumentException("Payment not found: ${request.paymentId}") }

            val gateway = getPaymentGateway(payment.paymentGateway ?: "STRIPE")
            val gatewayResponse = gateway.refundPayment(payment.externalTransactionId ?: payment.paymentId, request.amount)

            if (gatewayResponse.success) {
                val updatedPayment = payment.copy(
                    status = PaymentStatus.REFUNDED,
                    updatedAt = LocalDateTime.now()
                )
                paymentRepository.save(updatedPayment)
            }

            PaymentResponse(
                success = gatewayResponse.success,
                paymentId = payment.paymentId,
                externalTransactionId = payment.externalTransactionId,
                status = if (gatewayResponse.success) "REFUNDED" else "FAILED",
                paymentType = payment.type.name,
                amount = gatewayResponse.amount ?: payment.amount,
                currency = "USD",
                paymentGateway = payment.paymentGateway,
                errorMessage = gatewayResponse.errorMessage,
                errorCode = gatewayResponse.errorCode
            )
        } catch (e: Exception) {
            PaymentResponse(
                success = false,
                paymentId = request.paymentId,
                externalTransactionId = null,
                status = "FAILED",
                paymentType = "ONE_TIME",
                amount = request.amount,
                currency = null,
                paymentGateway = null,
                errorMessage = e.message,
                errorCode = "INTERNAL_ERROR"
            )
        }
    }

    override suspend fun retrievePayment(paymentId: String): PaymentResponse {
        return try {
            val payment = paymentRepository.findByPaymentId(paymentId)
                .orElseThrow { IllegalArgumentException("Payment not found: $paymentId") }

            PaymentResponse(
                success = true,
                paymentId = payment.paymentId,
                externalTransactionId = payment.externalTransactionId,
                status = payment.status.name,
                paymentType = payment.type.name,
                amount = payment.amount,
                currency = "USD",
                paymentGateway = payment.paymentGateway,
                createdAt = payment.createdAt,
                processedAt = payment.processedAt
            )
        } catch (e: Exception) {
            PaymentResponse(
                success = false,
                paymentId = paymentId,
                externalTransactionId = null,
                status = "NOT_FOUND",
                paymentType = "ONE_TIME",
                amount = null,
                currency = null,
                paymentGateway = null,
                errorMessage = e.message,
                errorCode = "PAYMENT_NOT_FOUND"
            )
        }
    }

    override fun getPaymentsByUser(userId: Long): List<Payment> {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    override fun getPaymentsByStatus(status: PaymentStatus): List<Payment> {
        return paymentRepository.findByStatus(status)
    }

    override suspend fun cancelSubscription(request: SubscriptionCancelRequest): PaymentResponse {
        return try {
            val payment = paymentRepository.findByExternalTransactionId(request.subscriptionId)
                .orElse(paymentRepository.findAll().find { it.externalSubscriptionId == request.subscriptionId })
                ?: throw IllegalArgumentException("Subscription not found: ${request.subscriptionId}")

            val gateway = getPaymentGateway(payment.paymentGateway ?: "STRIPE")
            val gatewayResponse = gateway.cancelSubscription(request.subscriptionId, request.cancelAtPeriodEnd)

            PaymentResponse(
                success = gatewayResponse.success,
                paymentId = payment.paymentId,
                externalTransactionId = payment.externalTransactionId,
                externalSubscriptionId = payment.externalSubscriptionId,
                status = gatewayResponse.status,
                paymentType = payment.type.name,
                amount = payment.amount,
                currency = "USD",
                paymentGateway = payment.paymentGateway,
                subscriptionDetails = gatewayResponse.subscriptionDetails?.let { details ->
                    SubscriptionDetails(
                        subscriptionId = details.subscriptionId,
                        status = details.status,
                        currentPeriodStart = details.currentPeriodStart,
                        currentPeriodEnd = details.currentPeriodEnd,
                        interval = details.interval,
                        intervalCount = details.intervalCount,
                        trialEnd = details.trialEnd,
                        cancelAtPeriodEnd = details.cancelAtPeriodEnd
                    )
                },
                errorMessage = gatewayResponse.errorMessage,
                errorCode = gatewayResponse.errorCode
            )
        } catch (e: Exception) {
            PaymentResponse(
                success = false,
                paymentId = null,
                externalTransactionId = null,
                status = "FAILED",
                paymentType = "RECURRING",
                amount = null,
                currency = null,
                paymentGateway = null,
                errorMessage = e.message,
                errorCode = "INTERNAL_ERROR"
            )
        }
    }

    override suspend fun updateSubscription(request: SubscriptionUpdateRequest): PaymentResponse {
        return try {
            val payment = paymentRepository.findByExternalTransactionId(request.subscriptionId)
                .orElse(paymentRepository.findAll().find { it.externalSubscriptionId == request.subscriptionId })
                ?: throw IllegalArgumentException("Subscription not found: ${request.subscriptionId}")

            val gateway = getPaymentGateway(payment.paymentGateway ?: "STRIPE")
            val gatewayUpdateRequest = GatewaySubscriptionUpdateRequest(
                amount = request.amount,
                paymentMethodId = request.paymentMethodId,
                metadata = request.metadata
            )
            val gatewayResponse = gateway.updateSubscription(request.subscriptionId, gatewayUpdateRequest)

            PaymentResponse(
                success = gatewayResponse.success,
                paymentId = payment.paymentId,
                externalTransactionId = payment.externalTransactionId,
                externalSubscriptionId = payment.externalSubscriptionId,
                status = gatewayResponse.status,
                paymentType = payment.type.name,
                amount = gatewayResponse.amount ?: payment.amount,
                currency = "USD",
                paymentGateway = payment.paymentGateway,
                subscriptionDetails = gatewayResponse.subscriptionDetails?.let { details ->
                    SubscriptionDetails(
                        subscriptionId = details.subscriptionId,
                        status = details.status,
                        currentPeriodStart = details.currentPeriodStart,
                        currentPeriodEnd = details.currentPeriodEnd,
                        interval = details.interval,
                        intervalCount = details.intervalCount,
                        trialEnd = details.trialEnd,
                        cancelAtPeriodEnd = details.cancelAtPeriodEnd
                    )
                },
                errorMessage = gatewayResponse.errorMessage,
                errorCode = gatewayResponse.errorCode
            )
        } catch (e: Exception) {
            PaymentResponse(
                success = false,
                paymentId = null,
                externalTransactionId = null,
                status = "FAILED",
                paymentType = "RECURRING",
                amount = request.amount,
                currency = null,
                paymentGateway = null,
                errorMessage = e.message,
                errorCode = "INTERNAL_ERROR"
            )
        }
    }

    override suspend fun retrieveSubscription(subscriptionId: String): PaymentResponse {
        return try {
            val payment = paymentRepository.findByExternalTransactionId(subscriptionId)
                .orElse(paymentRepository.findAll().find { it.externalSubscriptionId == subscriptionId })
                ?: throw IllegalArgumentException("Subscription not found: $subscriptionId")

            val gateway = getPaymentGateway(payment.paymentGateway ?: "STRIPE")
            val gatewayResponse = gateway.retrieveSubscription(subscriptionId)

            PaymentResponse(
                success = gatewayResponse.success,
                paymentId = payment.paymentId,
                externalTransactionId = payment.externalTransactionId,
                externalSubscriptionId = payment.externalSubscriptionId,
                status = gatewayResponse.status,
                paymentType = payment.type.name,
                amount = payment.amount,
                currency = "USD",
                paymentGateway = payment.paymentGateway,
                subscriptionDetails = gatewayResponse.subscriptionDetails?.let { details ->
                    SubscriptionDetails(
                        subscriptionId = details.subscriptionId,
                        status = details.status,
                        currentPeriodStart = details.currentPeriodStart,
                        currentPeriodEnd = details.currentPeriodEnd,
                        interval = details.interval,
                        intervalCount = details.intervalCount,
                        trialEnd = details.trialEnd,
                        cancelAtPeriodEnd = details.cancelAtPeriodEnd
                    )
                },
                createdAt = payment.createdAt,
                processedAt = payment.processedAt,
                errorMessage = gatewayResponse.errorMessage,
                errorCode = gatewayResponse.errorCode
            )
        } catch (e: Exception) {
            PaymentResponse(
                success = false,
                paymentId = null,
                externalTransactionId = null,
                status = "NOT_FOUND",
                paymentType = "RECURRING",
                amount = null,
                currency = null,
                paymentGateway = null,
                errorMessage = e.message,
                errorCode = "SUBSCRIPTION_NOT_FOUND"
            )
        }
    }

    override fun getSubscriptionPayments(subscriptionId: Long): List<Payment> {
        return paymentRepository.findAll().filter { it.subscription?.id == subscriptionId }
    }

    private fun mapGatewayStatusToPaymentStatus(gatewayStatus: String): PaymentStatus {
        return when (gatewayStatus.lowercase()) {
            "requires_confirmation", "created" -> PaymentStatus.PENDING
            "processing" -> PaymentStatus.PROCESSING
            "succeeded", "completed" -> PaymentStatus.COMPLETED
            "failed" -> PaymentStatus.FAILED
            "canceled", "cancelled" -> PaymentStatus.CANCELLED
            "refunded" -> PaymentStatus.REFUNDED
            else -> PaymentStatus.PENDING
        }
    }
}