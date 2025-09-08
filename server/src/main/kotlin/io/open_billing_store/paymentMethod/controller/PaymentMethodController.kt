package io.open_billing_store.paymentMethod.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payment-methods")
class PaymentMethodController {

    /**
     * 지원하는 결제 게이트웨이 목록 조회
     * 현재 시스템에서 지원하는 결제 게이트웨이들을 반환합니다.
     * 
     * Get supported gateways
     * Returns list of payment gateways supported by the current system.
     */
    @GetMapping("/available/gateways")
    fun getSupportedGateways(): ResponseEntity<List<String>> {
        val supportedGateways = listOf("STRIPE", "PAYPAL")
        return ResponseEntity.ok(supportedGateways)
    }
}