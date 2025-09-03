package io.open_billing_store.dummydata.controller

import io.open_billing_store.entity.*
import io.open_billing_store.repository.*
import io.open_billing_store.util.CommonUtils
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@RestController
@RequestMapping("/api/dummy-data")
class DummyDataController(
    private val serviceRepository: ServiceRepository,
    private val categoryRepository: CategoryRepository,
    private val countryRepository: CountryRepository,
    private val productRepository: ProductRepository,
    private val productPriceRepository: ProductPriceByCountryRepository,
    private val userRepository: UserRepository,
    private val orderRepository: OrderRepository,
    private val commonUtils: CommonUtils
) {

    @PostMapping("/generate-all")
    fun generateAllDummyData(): ResponseEntity<Map<String, Any>> {
        val results = mutableMapOf<String, Any>()
        
        try {
            val services = createDummyServices()
            results["services"] = services.size
            
            val categories = createDummyCategories()
            results["categories"] = categories.size
            
            val countries = createDummyCountries()
            results["countries"] = countries.size
            
            val products = createDummyProducts(services.map { it.serviceId })
            results["products"] = products.size
            
            val productPrices = createDummyProductPrices(products.map { it.productId }, countries.map { it.countryCode })
            results["productPrices"] = productPrices.size
            
            val users = createDummyUsers(services.map { it.serviceId })
            results["users"] = users.size
            
            val orders = createDummyOrders(users.map { it.id }, services.map { it.serviceId }, products.map { it.productId })
            results["orders"] = orders.size
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Dummy data generated successfully",
                "data" to results
            ))
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Error generating dummy data: ${e.message}"
            ))
        }
    }

    @PostMapping("/services")
    fun createDummyServices(): List<Service> {
        val services = listOf(
            Service(
                serviceId = "SRV001",
                serviceName = "E-commerce Platform",
                description = "Online store management platform",
                apiKey = "api_key_${UUID.randomUUID()}",
                status = ServiceStatus.ACTIVE
            ),
            Service(
                serviceId = "SRV002", 
                serviceName = "CRM System",
                description = "Customer relationship management system",
                apiKey = "api_key_${UUID.randomUUID()}",
                status = ServiceStatus.ACTIVE
            ),
            Service(
                serviceId = "SRV003",
                serviceName = "Analytics Tool",
                description = "Business analytics and reporting tool",
                apiKey = "api_key_${UUID.randomUUID()}",
                status = ServiceStatus.ACTIVE
            )
        )
        
        return serviceRepository.saveAll(services)
    }

    @PostMapping("/categories")
    fun createDummyCategories(): List<Category> {
        val categories = listOf(
            Category(name = "Software", description = "Software products and services"),
            Category(name = "Hardware", description = "Physical products and equipment"),
            Category(name = "Consulting", description = "Professional consulting services"),
            Category(name = "Support", description = "Technical support and maintenance"),
            Category(name = "Training", description = "Training and educational services")
        )
        
        return categoryRepository.saveAll(categories)
    }

    @PostMapping("/countries")
    fun createDummyCountries(): List<Country> {
        val countries = listOf(
            Country(
                countryCode = "US",
                countryName = "United States",
                currencyCode = "USD",
                taxRate = BigDecimal("0.0875")
            ),
            Country(
                countryCode = "KR",
                countryName = "South Korea", 
                currencyCode = "KRW",
                taxRate = BigDecimal("0.1000")
            ),
            Country(
                countryCode = "GB",
                countryName = "United Kingdom",
                currencyCode = "GBP", 
                taxRate = BigDecimal("0.2000")
            ),
            Country(
                countryCode = "JP",
                countryName = "Japan",
                currencyCode = "JPY",
                taxRate = BigDecimal("0.1000")
            ),
            Country(
                countryCode = "DE",
                countryName = "Germany",
                currencyCode = "EUR",
                taxRate = BigDecimal("0.1900")
            )
        )
        
        return countryRepository.saveAll(countries)
    }

    @PostMapping("/products")
    fun createDummyProducts(@RequestParam(required = false) serviceIds: List<String>? = null): List<Product> {
        val services = if (serviceIds != null) {
            serviceRepository.findAllById(serviceIds)
        } else {
            serviceRepository.findAll()
        }
        
        val categories = categoryRepository.findAll()
        
        if (services.isEmpty()) {
            throw IllegalStateException("No services found. Please create services first.")
        }
        
        val products = mutableListOf<Product>()
        
        services.forEach { service ->
            products.addAll(listOf(
                Product(
                    productId = "PRD${service.serviceId}001",
                    service = service,
                    name = "${service.serviceName} Basic Plan",
                    description = "Basic features for ${service.serviceName}",
                    category = categories.firstOrNull(),
                    type = ProductType.SUBSCRIPTION_SERVICE,
                    billingInterval = BillingInterval.MONTHLY
                ),
                Product(
                    productId = "PRD${service.serviceId}002",
                    service = service,
                    name = "${service.serviceName} Pro Plan", 
                    description = "Advanced features for ${service.serviceName}",
                    category = categories.firstOrNull(),
                    type = ProductType.SUBSCRIPTION_SERVICE,
                    billingInterval = BillingInterval.MONTHLY
                ),
                Product(
                    productId = "PRD${service.serviceId}003",
                    service = service,
                    name = "${service.serviceName} Setup",
                    description = "One-time setup service for ${service.serviceName}",
                    category = categories.getOrNull(2),
                    type = ProductType.ONE_TIME_SERVICE
                )
            ))
        }
        
        return productRepository.saveAll(products)
    }

    @PostMapping("/product-prices")
    fun createDummyProductPrices(
        @RequestParam(required = false) productIds: List<String>? = null,
        @RequestParam(required = false) countryCodes: List<String>? = null
    ): List<ProductPriceByCountry> {
        val products = if (productIds != null) {
            productRepository.findAllById(productIds)
        } else {
            productRepository.findAll()
        }
        
        val countries = if (countryCodes != null) {
            countryRepository.findByCountryCodeIn(countryCodes)
        } else {
            countryRepository.findAll()
        }
        
        val prices = mutableListOf<ProductPriceByCountry>()
        
        products.forEach { product ->
            countries.forEach { country ->
                val basePrice = when {
                    product.name.contains("Basic") -> BigDecimal("29.99")
                    product.name.contains("Pro") -> BigDecimal("99.99")
                    product.name.contains("Setup") -> BigDecimal("199.99")
                    else -> BigDecimal("49.99")
                }
                
                val localizedPrice = when (country.currencyCode) {
                    "USD" -> basePrice
                    "KRW" -> basePrice.multiply(BigDecimal("1300"))
                    "GBP" -> basePrice.multiply(BigDecimal("0.8"))
                    "JPY" -> basePrice.multiply(BigDecimal("110"))
                    "EUR" -> basePrice.multiply(BigDecimal("0.85"))
                    else -> basePrice
                }
                
                prices.add(
                    ProductPriceByCountry(
                        product = product,
                        countryCode = country.countryCode,
                        countryName = country.countryName,
                        price = localizedPrice,
                        currencyCode = country.currencyCode
                    )
                )
            }
        }
        
        return productPriceRepository.saveAll(prices)
    }

    @PostMapping("/users")
    fun createDummyUsers(@RequestParam(required = false) serviceIds: List<String>? = null): List<User> {
        val services = if (serviceIds != null) {
            serviceRepository.findAllById(serviceIds)
        } else {
            serviceRepository.findAll()
        }
        
        if (services.isEmpty()) {
            throw IllegalStateException("No services found. Please create services first.")
        }
        
        val users = mutableListOf<User>()
        val firstNames = listOf("John", "Jane", "Bob", "Alice", "Charlie", "Diana", "Eve", "Frank")
        val lastNames = listOf("Smith", "Johnson", "Brown", "Davis", "Wilson", "Miller", "Moore", "Taylor")
        
        services.forEach { service ->
            repeat(5) { i ->
                val firstName = firstNames.random()
                val lastName = lastNames.random()
                users.add(
                    User(
                        userId = "USER${service.serviceId}${String.format("%03d", i + 1)}",
                        service = service,
                        email = "${firstName.lowercase()}.${lastName.lowercase()}${i}@example.com",
                        firstName = firstName,
                        lastName = lastName,
                        phoneNumber = "+1-555-${String.format("%04d", (1000..9999).random())}",
                        role = if (i == 0) UserRole.ADMIN else UserRole.CUSTOMER
                    )
                )
            }
        }
        
        return userRepository.saveAll(users)
    }

    @PostMapping("/orders")
    fun createDummyOrders(
        @RequestParam(required = false) userIds: List<Long>? = null,
        @RequestParam(required = false) serviceIds: List<String>? = null,
        @RequestParam(required = false) productIds: List<String>? = null
    ): List<Order> {
        val users = if (userIds != null) {
            userRepository.findAllById(userIds)
        } else {
            userRepository.findAll()
        }
        
        val services = if (serviceIds != null) {
            serviceRepository.findAllById(serviceIds)
        } else {
            serviceRepository.findAll()
        }
        
        val products = if (productIds != null) {
            productRepository.findAllById(productIds)
        } else {
            productRepository.findAll()
        }
        
        if (users.isEmpty() || services.isEmpty() || products.isEmpty()) {
            throw IllegalStateException("Users, services, and products must exist before creating orders.")
        }
        
        val orders = mutableListOf<Order>()
        
        users.take(10).forEach { user ->
            val userProducts = products.filter { it.service.serviceId == user.service.serviceId }
            if (userProducts.isNotEmpty()) {
                val product = userProducts.random()
                val productPrice = BigDecimal("${(50..500).random()}.${(10..99).random()}")
                val taxAmount = productPrice.multiply(BigDecimal("0.1"))
                val totalAmount = productPrice.plus(taxAmount)
                
                orders.add(
                    Order(
                        orderNumber = commonUtils.generateOrderNumber(),
                        user = user,
                        service = user.service,
                        product = product,
                        currencyCode = "USD",
                        productPrice = productPrice,
                        totalAmount = totalAmount,
                        taxAmount = taxAmount,
                        status = listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PAID).random(),
                        type = if (product.type == ProductType.SUBSCRIPTION_SERVICE) OrderType.SUBSCRIPTION_BILLING else OrderType.ONE_TIME,
                        dueDate = if (product.type == ProductType.SUBSCRIPTION_SERVICE) LocalDate.now().plusDays(30) else null
                    )
                )
            }
        }
        
        return orderRepository.saveAll(orders)
    }

    @DeleteMapping("/clear-all")
    fun clearAllData(): ResponseEntity<Map<String, Any>> {
        return try {
            orderRepository.deleteAll()
            productPriceRepository.deleteAll()
            userRepository.deleteAll()
            productRepository.deleteAll()
            categoryRepository.deleteAll()
            countryRepository.deleteAll()
            serviceRepository.deleteAll()
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "All dummy data cleared successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Error clearing data: ${e.message}"
            ))
        }
    }
}