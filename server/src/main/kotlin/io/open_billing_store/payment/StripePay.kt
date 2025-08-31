package io.open_billing_store.payment

import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class StripePay : Pay() {
    
    override fun getPaymentMethodName(): String = "STRIPE"
    
    override fun processPayment(
        amount: BigDecimal,
        currencyCode: String,
        orderNumber: String,
        customerInfo: Map<String, Any>
    ): PaymentResult {
        if (!validateAmount(amount) || !validateCurrency(currencyCode)) {
            return PaymentResult(
                success = false,
                errorMessage = "Invalid amount or currency"
            )
        }
        
        return try {
            val transactionId = generateTransactionId()
            val success = simulateStripePayment(amount)
            
            if (success) {
                PaymentResult(
                    success = true,
                    transactionId = transactionId,
                    additionalData = mapOf(
                        "charge_id" to "ch_$transactionId",
                        "receipt_url" to "https://pay.stripe.com/receipts/$transactionId"
                    )
                )
            } else {
                PaymentResult(
                    success = false,
                    errorMessage = "Card declined"
                )
            }
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "Stripe API error: ${e.message}"
            )
        }
    }
    
    override fun refundPayment(transactionId: String, amount: BigDecimal, reason: String): PaymentResult {
        return try {
            val refundId = generateTransactionId()
            PaymentResult(
                success = true,
                transactionId = refundId,
                additionalData = mapOf(
                    "refund_id" to "re_$refundId",
                    "reason" to reason
                )
            )
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "Refund failed: ${e.message}"
            )
        }
    }
    
    override fun getPaymentStatus(transactionId: String): PaymentStatusResult {
        return PaymentStatusResult(
            transactionId = transactionId,
            status = "completed",
            completedAt = System.currentTimeMillis().toString()
        )
    }
    
    override fun generatePaymentUrl(amount: BigDecimal, currencyCode: String, orderNumber: String): String {
        return "https://checkout.stripe.com/pay?amount=$amount&currency=$currencyCode&order=$orderNumber"
    }
    
    private fun simulateStripePayment(amount: BigDecimal): Boolean {
        Thread.sleep(200)
        return Math.random() > 0.05
    }
}