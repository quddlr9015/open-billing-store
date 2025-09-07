# Open Billing Store

![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-purple.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green.svg)

An open-source billing and payment processing system designed for multi-service environments. Unlike traditional e-commerce platforms that focus on shopping cart functionality, Open Billing Store specializes in **one-time payments** and **subscription billing** for various digital services.

## 🎯 Why Open Billing Store?

- **Multi-Service Support**: One billing system to serve multiple different applications/services
- **No Shopping Cart**: Streamlined for direct purchases and subscription billing
- **Payment Gateway Agnostic**: Currently supports Stripe, PayPal with easy extensibility
- **Educational Value**: Great for understanding billing system architecture and implementation
- **Production Ready**: Includes comprehensive testing, monitoring, and security features

## ✨ Key Features

### Payment Processing
- **One-time payments** for digital products and services
- **Subscription billing** with flexible intervals (daily, weekly, monthly, quarterly, yearly)
- **Multiple payment gateways** (Stripe, PayPal) with unified API
- **Multi-currency support** (USD, EUR, KRW, etc.)
- **Tax calculation** with country-specific rules
- **Webhook support** for payment status updates

### Multi-Service Architecture
- **Service isolation**: Each service can have its own products and pricing
- **Country-specific pricing**: Different prices for different markets
- **Category management**: Organize products by categories
- **User management**: Centralized user system across services

### Developer Experience
- **RESTful APIs** with comprehensive documentation
- **Type-safe** Kotlin implementation
- **Extensive test coverage** with unit and integration tests
- **Docker support** for easy deployment
- **Multiple environments** (local, dev, prod) with proper configuration

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │    │  Payment APIs   │    │  Gateway APIs   │
│   ┌─────────┐   │    │  ┌───────────┐  │    │  ┌───────────┐  │
│   │ Service │   │────┤  │  Orders   │  │────┤  │  Stripe   │  │
│   │    A    │   │    │  └───────────┘  │    │  └───────────┘  │
│   └─────────┘   │    │  ┌───────────┐  │    │  ┌───────────┐  │
│   ┌─────────┐   │    │  │ Payments  │  │────┤  │  PayPal   │  │
│   │ Service │   │    │  └───────────┘  │    │  └───────────┘  │
│   │    B    │   │    │  ┌───────────┐  │    └─────────────────┘
│   └─────────┘   │    │  │Subscript. │  │
└─────────────────┘    │  └───────────┘  │
                       └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Docker (optional)
- MySQL 8.0+
- Redis (optional, for caching)

### Running Locally

1. **Clone the repository**
```bash
git clone https://github.com/your-username/open-billing-store.git
cd open-billing-store
```

2. **Configure the database**
```bash
# Copy and edit configuration
cp server/src/main/resources/application-local.properties.example server/src/main/resources/application-local.properties
```

Edit the configuration file with your database settings:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/billing_store
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Run the server**
```bash
cd server
./gradlew bootRunLocal
```

The server will start on `http://localhost:8080`

4. **Test the API**
```bash
# Check if the server is running
curl http://localhost:8080/actuator/health

# Create dummy data for testing
curl -X POST http://localhost:8080/api/dummy/create-all
```

### Using Docker

```bash
# Build and run with Docker Compose
docker-compose up -d
```

## 📚 API Documentation

### Core Endpoints

#### Create One-time Payment
```http
POST /api/payments/pay
Content-Type: application/json

{
    "userId": 1,
    "amount": 100.00,
    "currency": "USD",
    "paymentGateway": "STRIPE",
    "paymentType": "ONE_TIME",
    "orderId": 123,
    "paymentMethodId": "pm_card_visa"
}
```

#### Create Subscription
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
        "trialPeriodDays": 7
    }
}
```

#### Initialize Order
```http
POST /api/orders/init
Content-Type: application/json

{
    "userId": 1,
    "productId": "PROD001",
    "currencyCode": "USD",
    "couponNumber": "DISCOUNT10"
}
```

For complete API documentation, see [PAYMENT_API_GUIDE.md](server/PAYMENT_API_GUIDE.md)

## 🛠️ Technology Stack

### Backend
- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.5
- **Database**: MySQL 8.0 with JPA/Hibernate
- **Caching**: Redis (Spring Data Redis)
- **Security**: Spring Security
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Build Tool**: Gradle with Kotlin DSL

### Frontend (Client Examples)
- **HTML/CSS/JavaScript**: Universal payment form
- **Payment Integration**: NaverPay, Stripe, PayPal examples

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Monitoring**: Spring Boot Actuator
- **Message Queue**: Apache Kafka (Spring Kafka)
- **Internationalization**: ICU4J for currency handling

## 🧪 Testing

### Running Tests
```bash
# Run all tests
./gradlew test

# Run with local profile
./gradlew testLocal

# Generate test report
./gradlew test jacocoTestReport
```

### Test Coverage
- **Unit Tests**: Service layer business logic
- **Integration Tests**: API endpoints and database operations  
- **Payment Gateway Tests**: Mock and integration tests for Stripe/PayPal
- **Tax Calculation Tests**: Country-specific tax rule validation

## 📦 Deployment

### Environment Configurations

#### Local Development
```bash
./gradlew bootRunLocal
```

#### Development Environment
```bash
./gradlew bootRunDev
```

#### Production Environment
```bash
./gradlew bootRunProd
```

### Environment Variables
```bash
# Database
DB_URL=jdbc:mysql://localhost:3306/billing_store
DB_USERNAME=billing_user
DB_PASSWORD=your_secure_password

# Payment Gateways
STRIPE_SECRET_KEY=sk_live_...
STRIPE_PUBLISHABLE_KEY=pk_live_...
PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_CLIENT_SECRET=your_paypal_secret

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka (optional)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 🔐 Security Features

- **Input Validation**: Comprehensive validation for all API endpoints
- **SQL Injection Protection**: JPA/Hibernate with parameterized queries
- **Authentication & Authorization**: Spring Security integration
- **Payment Data Encryption**: Sensitive data encryption at rest
- **Webhook Signature Verification**: Secure webhook payload validation
- **Rate Limiting**: API request rate limiting
- **CORS Configuration**: Cross-origin request security

## 📊 Monitoring & Analytics

### Built-in Monitoring
- **Health Checks**: `/actuator/health`
- **Metrics**: `/actuator/metrics`
- **Application Info**: `/actuator/info`
- **Database Status**: Connection pool and query metrics

### Custom Metrics
- Payment success/failure rates
- Subscription churn analysis
- Gateway performance monitoring  
- Tax calculation accuracy

## 🔧 Configuration

### Application Profiles

#### Local (`application-local.properties`)
- H2 in-memory database for quick development
- Debug logging enabled
- Mock payment gateways

#### Development (`application-dev.properties`)
- MySQL database connection
- Detailed logging
- Sandbox payment gateway configurations

#### Production (`application-prod.properties`)
- Production database with connection pooling
- Error-level logging only
- Live payment gateway configurations
- Security hardening

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

### Development Setup
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/awesome-feature`)
3. Make your changes and add tests
4. Ensure all tests pass (`./gradlew test`)
5. Commit your changes (`git commit -m 'Add awesome feature'`)
6. Push to the branch (`git push origin feature/awesome-feature`)
7. Open a Pull Request

### Code Standards
- Follow Kotlin coding conventions
- Maintain test coverage above 80%
- Update documentation for API changes
- Use meaningful commit messages

## 📋 Use Cases

### SaaS Subscription Billing
Perfect for software-as-a-service applications requiring recurring billing:
- Monthly/annual subscription plans
- Free trial periods
- Plan upgrades and downgrades
- Usage-based billing

### Digital Product Sales
Ideal for selling digital products and services:
- E-books, courses, software licenses
- One-time service purchases
- Multi-tier pricing by country
- Instant digital delivery

### Multi-Service Platform
Great for organizations running multiple services:
- Centralized billing across services
- Unified user management
- Service-specific product catalogs
- Cross-service analytics

### Educational Projects
Excellent for learning about:
- Payment processing architecture
- Subscription billing systems
- Multi-tenant applications
- API design and security

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - The amazing framework
- [Stripe](https://stripe.com) - Payment processing infrastructure
- [PayPal](https://developer.paypal.com) - Global payment solutions
- [Kotlin](https://kotlinlang.org) - Concise and safe programming language

## 📞 Support

- **Documentation**: [Full API Guide](server/PAYMENT_API_GUIDE.md)
- **Issues**: [GitHub Issues](https://github.com/your-username/open-billing-store/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-username/open-billing-store/discussions)
- **Email**: your.email@example.com

---

**Open Billing Store** - Building the future of billing systems, one payment at a time. 💳✨
