package io.open_billing_store.order.response

data class OrderInitResponse(
    val resultCode: String,
    val orderId: String,
    val productPrice: String,
    val displayProductPrice: String,
    val taxAmount: String,
    val displayTaxAmount: String,
    val totalPaymentAmount: String,
    val displayTotalPaymentAmount: String,
    val isFreeTrial: Boolean,
    val currencyCode: String,


    )