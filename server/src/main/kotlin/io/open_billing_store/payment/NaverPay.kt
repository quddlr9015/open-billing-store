package io.open_billing_store.payment

import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class NaverPay : Pay() {
    
    override fun getPaymentMethodName(): String = "NAVERPAY"
    
    override fun processPayment(
        amount: BigDecimal,
        currencyCode: String,
        orderNumber: String,
        customerInfo: Map<String, Any>
    ): PaymentResult {
        if (!validateAmount(amount) || !validateCurrency(currencyCode)) {
            return PaymentResult(
                success = false,
                errorMessage = "올바르지 않은 결제 정보입니다"
            )
        }
        
        return try {
            val transactionId = generateTransactionId()
            val success = simulateNaverPayment(amount)
            
            if (success) {
                PaymentResult(
                    success = true,
                    transactionId = transactionId,
                    additionalData = mapOf(
                        "paymentId" to "npay_$transactionId",
                        "admissionYmdt" to System.currentTimeMillis(),
                        "totalPayAmount" to amount
                    )
                )
            } else {
                PaymentResult(
                    success = false,
                    errorMessage = "네이버페이 결제에 실패했습니다"
                )
            }
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "네이버페이 API 오류: ${e.message}"
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
                    "cancelYmdt" to System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            PaymentResult(
                success = false,
                errorMessage = "환불 처리 실패: ${e.message}"
            )
        }
    }
    
    override fun getPaymentStatus(transactionId: String): PaymentStatusResult {
        return PaymentStatusResult(
            transactionId = transactionId,
            status = "SUCCESS",
            completedAt = System.currentTimeMillis().toString()
        )
    }
    
    override fun generatePaymentUrl(amount: BigDecimal, currencyCode: String, orderNumber: String): String {
        return "https://order.pay.naver.com/payments?orderId=$orderNumber&amount=$amount"
    }
    
    private fun simulateNaverPayment(amount: BigDecimal): Boolean {
        Thread.sleep(180)
        return Math.random() > 0.07
    }
}