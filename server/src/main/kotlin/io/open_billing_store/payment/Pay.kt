package io.open_billing_store.payment

import io.open_billing_store.entity.Payment
import java.math.BigDecimal

abstract class Pay {
    
    abstract fun getPaymentMethodName(): String
    
    abstract fun processPayment(
        amount: BigDecimal,
        currencyCode: String,
        orderNumber: String,
        customerInfo: Map<String, Any>
    ): PaymentResult
    
    abstract fun refundPayment(
        transactionId: String,
        amount: BigDecimal,
        reason: String
    ): PaymentResult
    
    abstract fun getPaymentStatus(transactionId: String): PaymentStatusResult
    
    abstract fun generatePaymentUrl(
        amount: BigDecimal,
        currencyCode: String,
        orderNumber: String
    ): String
    
    protected fun generateTransactionId(): String {
        return "${getPaymentMethodName()}_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(8)}"
    }
    
    protected fun validateAmount(amount: BigDecimal): Boolean {
        return amount > BigDecimal.ZERO
    }
    
    protected fun validateCurrency(currencyCode: String): Boolean {
        val supportedCurrencies = listOf("KRW", "USD", "EUR", "JPY")
        return currencyCode in supportedCurrencies
    }
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val errorMessage: String? = null,
    val additionalData: Map<String, Any> = emptyMap()
)

data class PaymentStatusResult(
    val transactionId: String,
    val status: String,
    val amount: BigDecimal? = null,
    val currencyCode: String? = null,
    val completedAt: String? = null
)