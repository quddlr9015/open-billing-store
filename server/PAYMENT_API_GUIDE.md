# Payment API Guide

This payment API supports multiple payment gateways (currently Stripe and PayPal) and handles both **one-time** and **recurring payments** through a unified interface.

## Architecture

### Core Components

1. **PaymentGateway Interface** - Abstraction for all payment providers
2. **PaymentService** - Business logic layer
3. **PaymentController** - REST API endpoints
4. **Gateway Implementations** - Provider-specific implementations

### Supported Payment Gateways

- **Stripe** - Credit card processing & subscriptions
- **PayPal** - PayPal payments & subscriptions
- Easily extensible for additional gateways

### Payment Types

- **ONE_TIME** - Single payment transactions
- **RECURRING** - Subscription-based payments

## API Endpoints

### Create One-Time Payment
```http
POST /api/payments/pay
Content-Type: application/json

{
    "userId": 1,
    "amount": 100.00,
    "currency": "USD",
    "paymentGateway": "STRIPE",
    "paymentType": "ONE_TIME",
    "paymentMethodId": "pm_card_visa",
    "orderId": 123,
    "metadata": {
        "order_type": "purchase"
    }
}
```

### Create Recurring Payment (Subscription)
```http
POST /api/payments/pay
Content-Type: application/json

{
    "userId": 1,
    "amount": 29.99,
    "currency": "USD",
    "paymentGateway": "STRIPE",
    "paymentType": "RECURRING",
    "paymentMethodId": "pm_card_visa",
    "subscriptionPlan": {
        "interval": "month",
        "intervalCount": 1,
        "trialPeriodDays": 7,
        "description": "Premium Monthly Plan"
    },
    "metadata": {
        "plan": "premium"
    }
}
```

### Confirm Payment
```http
POST /api/payments/{paymentId}/confirm
Content-Type: application/json

{
    "paymentMethodId": "pm_card_visa"
}
```

### Cancel Payment
```http
POST /api/payments/{paymentId}/cancel
```

### Refund Payment
```http
POST /api/payments/{paymentId}/refund
Content-Type: application/json

{
    "amount": 50.00,
    "reason": "Customer request"
}
```

### Get Payment Details
```http
GET /api/payments/{paymentId}
```

### Get User Payments
```http
GET /api/payments/user/{userId}
```

### Get Payments by Status
```http
GET /api/payments/status/COMPLETED
```

### Subscription Management

#### Cancel Subscription
```http
POST /api/payments/subscriptions/{subscriptionId}/cancel
Content-Type: application/json

{
    "cancelAtPeriodEnd": true,
    "reason": "Customer request"
}
```

#### Update Subscription
```http
PUT /api/payments/subscriptions/{subscriptionId}
Content-Type: application/json

{
    "amount": 39.99,
    "paymentMethodId": "pm_new_card",
    "metadata": {
        "plan_upgrade": "premium_plus"
    }
}
```

#### Get Subscription Details
```http
GET /api/payments/subscriptions/{subscriptionId}
```

#### Get Subscription Payment History
```http
GET /api/payments/subscriptions/{subscriptionId}/payments
```

### Get Supported Gateways
```http
GET /api/payments/gateways
```

### Get Supported Payment Types
```http
GET /api/payments/types
```

## Payment Flows

### One-Time Payment Flow
1. **Create Payment** - Initialize payment with chosen gateway
2. **Confirm Payment** - Complete the payment process
3. **Handle Response** - Process success/failure

### Recurring Payment Flow
1. **Create Subscription** - Set up recurring payment plan
2. **Automatic Billing** - Gateway handles recurring charges
3. **Manage Subscription** - Update, cancel, or retrieve subscription
4. **Handle Webhooks** - Process payment success/failure notifications

## Adding New Payment Gateways

1. Implement `PaymentGateway` interface
2. Add as Spring `@Component`
3. Update `PaymentMethod` enum
4. The service will automatically discover the new gateway

Example implementation:
```kotlin
@Component
class NewGateway : PaymentGateway {
    override fun getProviderName(): String = "NEW_GATEWAY"
    
    override suspend fun createPayment(request: PaymentRequest): PaymentGatewayResponse {
        // Implementation
    }
    
    // ... other methods
}
```

## Response Format

### Payment Response
```json
{
    "success": true,
    "paymentId": "pay_123",
    "externalTransactionId": "pi_stripe_123",
    "externalSubscriptionId": "sub_stripe_456",
    "status": "COMPLETED",
    "paymentType": "RECURRING",
    "amount": 29.99,
    "currency": "USD",
    "paymentGateway": "STRIPE",
    "subscriptionDetails": {
        "subscriptionId": "sub_stripe_456",
        "status": "active",
        "currentPeriodStart": "2025-09-07T02:00:00",
        "currentPeriodEnd": "2025-10-07T02:00:00",
        "interval": "month",
        "intervalCount": 1,
        "trialEnd": "2025-09-14T02:00:00",
        "cancelAtPeriodEnd": false
    },
    "createdAt": "2025-09-07T02:00:00",
    "processedAt": "2025-09-07T02:00:15"
}
```

### Error Response
```json
{
    "success": false,
    "paymentId": null,
    "status": "FAILED",
    "paymentType": "ONE_TIME",
    "errorMessage": "Card declined",
    "errorCode": "CARD_DECLINED"
}
```

## Payment Status Flow

- `PENDING` → Initial state for all payments
- `PROCESSING` → Payment being processed
- `COMPLETED` → Successfully processed
- `FAILED` → Payment failed
- `CANCELLED` → Payment cancelled
- `REFUNDED` → Payment refunded

## Subscription Status Flow

- `active` → Subscription is active and billing
- `past_due` → Payment failed, retrying
- `canceled` → Subscription cancelled
- `incomplete` → Initial payment failed
- `trialing` → In trial period

## Use Cases

### E-commerce Platform
```javascript
// One-time product purchase
const purchase = {
    userId: 123,
    amount: 99.99,
    paymentType: "ONE_TIME",
    paymentGateway: "STRIPE",
    orderId: 456,
    paymentMethodId: "pm_card_visa"
};

// Monthly subscription service
const subscription = {
    userId: 123,
    amount: 19.99,
    paymentType: "RECURRING",
    paymentGateway: "STRIPE",
    subscriptionPlan: {
        interval: "month",
        intervalCount: 1,
        trialPeriodDays: 14,
        description: "Premium Plan"
    }
};
```

### SaaS Application
```javascript
// Annual subscription with discount
const annualPlan = {
    userId: 456,
    amount: 199.99,
    paymentType: "RECURRING",
    paymentGateway: "PAYPAL",
    subscriptionPlan: {
        interval: "year",
        intervalCount: 1,
        description: "Annual Pro Plan (2 months free)"
    }
};

// Upgrade existing subscription
const upgrade = {
    subscriptionId: "sub_existing_123",
    amount: 49.99,
    metadata: {
        "previous_plan": "basic",
        "new_plan": "premium"
    }
};
```

## Integration Examples

### Frontend Integration (React)
```javascript
import axios from 'axios';

// Create one-time payment
const createPayment = async (paymentData) => {
    try {
        const response = await axios.post('/api/payments/pay', {
            userId: user.id,
            amount: 29.99,
            currency: 'USD',
            paymentGateway: 'STRIPE',
            paymentType: 'ONE_TIME',
            paymentMethodId: paymentMethod.id,
            metadata: {
                product_id: product.id,
                session_id: session.id
            }
        });
        
        if (response.data.success) {
            // Handle successful payment
            handlePaymentSuccess(response.data);
        } else {
            // Handle payment failure
            handlePaymentError(response.data);
        }
    } catch (error) {
        console.error('Payment failed:', error);
    }
};

// Create subscription
const createSubscription = async (subscriptionData) => {
    try {
        const response = await axios.post('/api/payments/pay', {
            userId: user.id,
            amount: 19.99,
            currency: 'USD',
            paymentGateway: 'STRIPE',
            paymentType: 'RECURRING',
            paymentMethodId: paymentMethod.id,
            subscriptionPlan: {
                interval: 'month',
                intervalCount: 1,
                trialPeriodDays: 7,
                description: 'Monthly Premium Plan'
            }
        });
        
        if (response.data.success) {
            // Store subscription details
            setSubscription(response.data.subscriptionDetails);
        }
    } catch (error) {
        console.error('Subscription creation failed:', error);
    }
};

// Cancel subscription
const cancelSubscription = async (subscriptionId, immediate = false) => {
    try {
        const response = await axios.post(
            `/api/payments/subscriptions/${subscriptionId}/cancel`,
            {
                cancelAtPeriodEnd: !immediate,
                reason: 'Customer request'
            }
        );
        
        if (response.data.success) {
            // Update UI to reflect cancellation
            updateSubscriptionStatus('canceled');
        }
    } catch (error) {
        console.error('Subscription cancellation failed:', error);
    }
};
```

### Backend Integration (Spring Boot)
```kotlin
@Service
class OrderService(
    private val paymentService: PaymentService
) {
    
    suspend fun processOrder(order: Order): OrderResult {
        val paymentRequest = PaymentCreateRequest(
            userId = order.userId,
            amount = order.total,
            currency = order.currency,
            paymentGateway = order.preferredGateway,
            paymentType = "ONE_TIME",
            orderId = order.id,
            paymentMethodId = order.paymentMethodId,
            metadata = mapOf(
                "order_number" to order.orderNumber,
                "items_count" to order.items.size.toString()
            )
        )
        
        val paymentResponse = paymentService.createPayment(paymentRequest)
        
        return if (paymentResponse.success) {
            // Update order status
            order.status = OrderStatus.PAID
            order.paymentId = paymentResponse.paymentId
            orderRepository.save(order)
            
            OrderResult.success(order, paymentResponse)
        } else {
            OrderResult.failure(paymentResponse.errorMessage)
        }
    }
    
    suspend fun createSubscription(
        userId: Long, 
        planId: String, 
        paymentMethodId: String
    ): SubscriptionResult {
        val plan = subscriptionPlanService.findById(planId)
        
        val subscriptionRequest = PaymentCreateRequest(
            userId = userId,
            amount = plan.amount,
            currency = plan.currency,
            paymentGateway = "STRIPE", // or determine based on user preference
            paymentType = "RECURRING",
            paymentMethodId = paymentMethodId,
            subscriptionPlan = SubscriptionPlanRequest(
                interval = plan.interval,
                intervalCount = plan.intervalCount,
                trialPeriodDays = plan.trialDays,
                description = plan.name
            )
        )
        
        val paymentResponse = paymentService.createPayment(subscriptionRequest)
        
        return if (paymentResponse.success) {
            // Create internal subscription record
            val subscription = Subscription(
                userId = userId,
                planId = planId,
                externalSubscriptionId = paymentResponse.externalSubscriptionId,
                status = "active",
                currentPeriodStart = paymentResponse.subscriptionDetails?.currentPeriodStart,
                currentPeriodEnd = paymentResponse.subscriptionDetails?.currentPeriodEnd
            )
            subscriptionRepository.save(subscription)
            
            SubscriptionResult.success(subscription, paymentResponse)
        } else {
            SubscriptionResult.failure(paymentResponse.errorMessage)
        }
    }
}
```

## Webhook Handling

### Payment Status Updates
```kotlin
@RestController
@RequestMapping("/api/webhooks")
class WebhookController(
    private val paymentService: PaymentService
) {
    
    @PostMapping("/stripe")
    fun handleStripeWebhook(@RequestBody payload: String, @RequestHeader headers: HttpHeaders): ResponseEntity<String> {
        // Verify webhook signature
        val event = parseStripeEvent(payload, headers)
        
        when (event.type) {
            "payment_intent.succeeded" -> {
                val paymentIntent = event.data.`object` as PaymentIntent
                updatePaymentStatus(paymentIntent.id, "COMPLETED")
            }
            "payment_intent.payment_failed" -> {
                val paymentIntent = event.data.`object` as PaymentIntent
                updatePaymentStatus(paymentIntent.id, "FAILED")
            }
            "invoice.payment_succeeded" -> {
                val invoice = event.data.`object` as Invoice
                recordSubscriptionPayment(invoice.subscription, invoice.amount_paid)
            }
            "customer.subscription.deleted" -> {
                val subscription = event.data.`object` as Subscription
                updateSubscriptionStatus(subscription.id, "canceled")
            }
        }
        
        return ResponseEntity.ok("Webhook processed")
    }
    
    private fun updatePaymentStatus(externalTransactionId: String, status: String) {
        // Update payment status in database
        // Notify user if needed
        // Trigger business logic (fulfill order, activate service, etc.)
    }
    
    private fun recordSubscriptionPayment(subscriptionId: String, amount: Long) {
        // Record successful subscription payment
        // Update subscription billing date
        // Send payment confirmation to user
    }
}
```

## Testing

### Unit Tests
```kotlin
@Test
fun `should create one-time payment successfully`() = runTest {
    // Given
    val request = PaymentCreateRequest(
        userId = 1L,
        amount = BigDecimal("100.00"),
        currency = "USD",
        paymentGateway = "STRIPE",
        paymentType = "ONE_TIME"
    )
    
    // When
    val response = paymentService.createPayment(request)
    
    // Then
    assertTrue(response.success)
    assertEquals("ONE_TIME", response.paymentType)
    assertEquals("PENDING", response.status)
    assertNotNull(response.paymentId)
}

@Test
fun `should create subscription successfully`() = runTest {
    // Given
    val request = PaymentCreateRequest(
        userId = 1L,
        amount = BigDecimal("29.99"),
        currency = "USD",
        paymentGateway = "STRIPE",
        paymentType = "RECURRING",
        subscriptionPlan = SubscriptionPlanRequest(
            interval = "month",
            intervalCount = 1,
            trialPeriodDays = 7
        )
    )
    
    // When
    val response = paymentService.createPayment(request)
    
    // Then
    assertTrue(response.success)
    assertEquals("RECURRING", response.paymentType)
    assertNotNull(response.externalSubscriptionId)
    assertNotNull(response.subscriptionDetails)
    assertEquals("month", response.subscriptionDetails?.interval)
}
```

### Integration Tests
```kotlin
@SpringBootTest
@AutoConfigureTestDatabase
class PaymentIntegrationTest {
    
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate
    
    @Test
    fun `should process complete payment flow`() {
        // Create payment
        val createRequest = PaymentCreateRequest(/* ... */)
        val createResponse = testRestTemplate.postForEntity(
            "/api/payments/pay",
            createRequest,
            PaymentResponse::class.java
        )
        
        assertEquals(HttpStatus.CREATED, createResponse.statusCode)
        assertTrue(createResponse.body?.success == true)
        
        val paymentId = createResponse.body?.paymentId
        
        // Confirm payment
        val confirmRequest = PaymentConfirmRequest(paymentId = paymentId!!)
        val confirmResponse = testRestTemplate.postForEntity(
            "/api/payments/$paymentId/confirm",
            confirmRequest,
            PaymentResponse::class.java
        )
        
        assertEquals(HttpStatus.OK, confirmResponse.statusCode)
        assertEquals("COMPLETED", confirmResponse.body?.status)
    }
}
```

## Error Handling & Troubleshooting

### Common Error Codes
- `CARD_DECLINED` - Payment method was declined
- `INSUFFICIENT_FUNDS` - Not enough balance
- `INVALID_PAYMENT_METHOD` - Payment method invalid or expired
- `SUBSCRIPTION_NOT_FOUND` - Subscription doesn't exist
- `GATEWAY_ERROR` - Payment gateway communication error
- `VALIDATION_ERROR` - Request validation failed
- `INTERNAL_ERROR` - Server-side error

### Retry Logic
```kotlin
suspend fun createPaymentWithRetry(request: PaymentCreateRequest, maxRetries: Int = 3): PaymentResponse {
    repeat(maxRetries) { attempt ->
        try {
            val response = paymentService.createPayment(request)
            if (response.success || !isRetryableError(response.errorCode)) {
                return response
            }
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) throw e
        }
        delay(1000 * (attempt + 1)) // Exponential backoff
    }
    throw PaymentException("Max retries exceeded")
}

private fun isRetryableError(errorCode: String?): Boolean {
    return errorCode in listOf("GATEWAY_ERROR", "NETWORK_ERROR", "TIMEOUT")
}
```

## Monitoring & Analytics

### Key Metrics to Track
- **Payment Success Rate** - Overall and by gateway
- **Subscription Churn Rate** - Monthly/annual cancellation rates
- **Revenue Metrics** - MRR, ARR, LTV
- **Payment Failure Reasons** - Categorized error analysis
- **Gateway Performance** - Response times and availability

### Logging Example
```kotlin
@Component
class PaymentEventLogger {
    
    fun logPaymentCreated(payment: Payment) {
        log.info(
            "Payment created: paymentId={}, userId={}, amount={}, type={}, gateway={}",
            payment.paymentId,
            payment.user.id,
            payment.amount,
            payment.type,
            payment.paymentGateway
        )
    }
    
    fun logSubscriptionEvent(event: String, subscriptionId: String, details: Map<String, Any>) {
        log.info(
            "Subscription event: event={}, subscriptionId={}, details={}",
            event,
            subscriptionId,
            details
        )
    }
}
```

## Security Considerations

### API Security
- **Authentication** - Require valid API keys or JWT tokens
- **Rate Limiting** - Prevent abuse with configurable limits
- **Input Validation** - Validate all request parameters
- **HTTPS Only** - Never process payments over HTTP
- **PCI Compliance** - Follow PCI DSS standards

### Data Protection
- **Encryption** - Encrypt sensitive data at rest and in transit
- **Token Storage** - Store payment method tokens securely
- **Audit Logging** - Log all payment operations
- **Data Retention** - Implement appropriate retention policies
- **Access Control** - Limit access to payment data

### Webhook Security
```kotlin
fun verifyWebhookSignature(payload: String, signature: String, secret: String): Boolean {
    val expectedSignature = computeHmacSha256(payload, secret)
    return constantTimeEquals(signature, expectedSignature)
}

private fun constantTimeEquals(a: String, b: String): Boolean {
    if (a.length != b.length) return false
    var result = 0
    for (i in a.indices) {
        result = result or (a[i].code xor b[i].code)
    }
    return result == 0
}
```

## Performance Optimization

### Caching Strategies
- Cache payment gateway configurations
- Cache user payment methods
- Cache subscription plan details
- Implement Redis for session management

### Database Optimization
- Index frequently queried fields (user_id, external_transaction_id)
- Partition large payment tables by date
- Use read replicas for reporting queries
- Implement connection pooling

### Async Processing
```kotlin
@Async("paymentTaskExecutor")
suspend fun processPaymentAsync(paymentId: String) {
    // Process payment notifications
    // Update related services
    // Send confirmation emails
}
```

This comprehensive guide now covers all aspects of implementing and using the enhanced payment API with both one-time and recurring payment capabilities.