package io.open_billing_store.payment

import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PayPal : Pay() {
    
    override fun getPaymentMethodName(): String = "PAYPAL"
    
    override fun processPayment(
        amount: BigDecimal,
        currencyCode: String,
        orderNumber: String,
        customerInfo: Map<String, Any>
    ): PaymentResult {
        if (!validateAmount(amount) || !validateCurrency(currencyCode)) {
            return PaymentResult(
                success = false,
                errorMessage = "Invalid payment details"
            )
        }
        
        return try {
            val transactionId = generateTransactionId()
            val success = simulatePayPalPayment(amount)
            
            if (success) {
                PaymentResult(
                    success = true,
                    transactionId = transactionId,
                    additionalData = mapOf(
                        "id" to "PAYID-$transactionId",
                        "create_time" to System.currentTimeMillis(),
                        "amount" to mapOf(
                            "total" to amount.toString(),
                            "currency" to currencyCode
                        ),
                        "state" to "approved"
                    )
                )
            } else {
                PaymentResult(
                    success = false,
                    errorMessage = "PayPal payment was declined"
                )
            }
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "PayPal API error: ${e.message}"
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
                    "refund_id" to "REFUND-$refundId",
                    "state" to "completed",
                    "reason" to reason
                )
            )
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "PayPal refund failed: ${e.message}"
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
        return "https://www.paypal.com/checkoutnow?amount=$amount&currency=$currencyCode&order=$orderNumber"
    }
    
    private fun simulatePayPalPayment(amount: BigDecimal): Boolean {
        Thread.sleep(250)
        return Math.random() > 0.06
    }
}