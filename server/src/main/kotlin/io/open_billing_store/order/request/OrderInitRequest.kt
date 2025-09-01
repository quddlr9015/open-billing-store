package io.open_billing_store.order.request

data class OrderInitRequest(
    val countryCode: String,
    val serviceId: String,
    val productId: String,
    val userId: String,

)