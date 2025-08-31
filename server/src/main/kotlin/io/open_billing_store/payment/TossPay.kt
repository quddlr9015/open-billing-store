package io.open_billing_store.payment

import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class TossPay : Pay() {
    
    override fun getPaymentMethodName(): String = "TOSS"
    
    override fun processPayment(
        amount: BigDecimal,
        currencyCode: String,
        orderNumber: String,
        customerInfo: Map<String, Any>
    ): PaymentResult {
        if (!validateAmount(amount) || !validateCurrency(currencyCode)) {
            return PaymentResult(
                success = false,
                errorMessage = "잘못된 금액 또는 통화입니다"
            )
        }
        
        return try {
            val transactionId = generateTransactionId()
            val success = simulateTossPayment(amount)
            
            if (success) {
                PaymentResult(
                    success = true,
                    transactionId = transactionId,
                    additionalData = mapOf(
                        "paymentKey" to "toss_$transactionId",
                        "orderId" to orderNumber,
                        "method" to "카드"
                    )
                )
            } else {
                PaymentResult(
                    success = false,
                    errorMessage = "결제가 거절되었습니다"
                )
            }
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "토스페이 API 오류: ${e.message}"
            )
        }
    }
    
    override fun refundPayment(transactionId: String, amount: BigDecimal, reason: String): PaymentResult {
        return try {
            val cancelId = generateTransactionId()
            PaymentResult(
                success = true,
                transactionId = cancelId,
                additionalData = mapOf(
                    "cancelAmount" to amount,
                    "cancelReason" to reason
                )
            )
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "환불 실패: ${e.message}"
            )
        }
    }
    
    override fun getPaymentStatus(transactionId: String): PaymentStatusResult {
        return PaymentStatusResult(
            transactionId = transactionId,
            status = "DONE",
            completedAt = System.currentTimeMillis().toString()
        )
    }
    
    override fun generatePaymentUrl(amount: BigDecimal, currencyCode: String, orderNumber: String): String {
        return "https://pay.toss.im/web/checkout?orderId=$orderNumber&amount=$amount"
    }
    
    private fun simulateTossPayment(amount: BigDecimal): Boolean {
        Thread.sleep(150)
        return Math.random() > 0.08
    }
}