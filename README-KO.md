# 오픈 빌링 스토어 (Open Billing Store)

![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.25-purple.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-green.svg)

다중 서비스 환경을 위해 설계된 오픈소스 빌링 및 결제 처리 시스템입니다. 장바구니 기능에 집중하는 기존의 전자상거래 플랫폼과 달리, 오픈 빌링 스토어는 다양한 디지털 서비스를 위한 **일회성 결제**와 **구독 청구**에 특화되어 있습니다.

## 🎯 왜 오픈 빌링 스토어인가?

- **다중 서비스 지원**: 여러 다른 애플리케이션/서비스를 지원하는 하나의 빌링 시스템
- **장바구니 없음**: 직접 구매와 구독 청구에 최적화된 간소화된 구조
- **결제 게이트웨이 무관**: 현재 Stripe, PayPal을 지원하며 쉬운 확장성 제공
- **교육적 가치**: 빌링 시스템 아키텍처와 구현 이해에 훌륭함
- **프로덕션 준비**: 포괄적인 테스팅, 모니터링, 보안 기능 포함

## ✨ 주요 기능

### 결제 처리
- 디지털 제품 및 서비스를 위한 **일회성 결제**
- 유연한 간격의 **구독 청구** (일간, 주간, 월간, 분기간, 연간)
- 통합 API를 통한 **다중 결제 게이트웨이** (Stripe, PayPal)
- **다중 통화 지원** (USD, EUR, KRW 등)
- 국가별 규칙이 적용된 **세금 계산**
- 결제 상태 업데이트를 위한 **웹훅 지원**

### 다중 서비스 아키텍처
- **서비스 격리**: 각 서비스가 고유한 제품과 가격 책정을 가질 수 있음
- **국가별 가격 책정**: 다른 시장을 위한 다른 가격
- **카테고리 관리**: 카테고리별 제품 구성
- **사용자 관리**: 서비스 간 중앙집중식 사용자 시스템

### 개발자 경험
- 포괄적인 문서화가 된 **RESTful API**
- **타입 안전한** Kotlin 구현
- 단위 및 통합 테스트를 포함한 **광범위한 테스트 커버리지**
- 쉬운 배포를 위한 **Docker 지원**
- 적절한 구성을 가진 **다중 환경** (local, dev, prod)

## 🏗️ 아키텍처

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

## 🚀 빠른 시작

### 사전 요구사항
- Java 21+
- Docker (선택사항)
- MySQL 8.0+
- Redis (선택사항, 캐싱용)

### 로컬에서 실행하기

1. **저장소 복제**
```bash
git clone https://github.com/your-username/open-billing-store.git
cd open-billing-store
```

2. **데이터베이스 설정**
```bash
# 설정 파일 복사 및 편집
cp server/src/main/resources/application-local.properties.example server/src/main/resources/application-local.properties
```

데이터베이스 설정으로 설정 파일을 편집하세요:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/billing_store
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **서버 실행**
```bash
cd server
./gradlew bootRunLocal
```

서버는 `http://localhost:8080`에서 시작됩니다.

4. **API 테스트**
```bash
# 서버가 실행 중인지 확인
curl http://localhost:8080/actuator/health

# 테스트용 더미 데이터 생성
curl -X POST http://localhost:8080/api/dummy/create-all
```

### Docker 사용하기

```bash
# Docker Compose로 빌드 및 실행
docker-compose up -d
```

## 📚 API 문서

### 핵심 엔드포인트

#### 일회성 결제 생성
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

#### 구독 생성
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

#### 주문 초기화
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

전체 API 문서는 [PAYMENT_API_GUIDE.md](server/PAYMENT_API_GUIDE.md)를 참조하세요.

## 🛠️ 기술 스택

### 백엔드
- **언어**: Kotlin 1.9.25
- **프레임워크**: Spring Boot 3.5.5
- **데이터베이스**: JPA/Hibernate를 사용한 MySQL 8.0
- **캐싱**: Redis (Spring Data Redis)
- **보안**: Spring Security
- **테스팅**: JUnit 5, Mockito, Testcontainers
- **빌드 도구**: Kotlin DSL을 사용한 Gradle

### 프론트엔드 (클라이언트 예제)
- **HTML/CSS/JavaScript**: 범용 결제 폼
- **결제 통합**: NaverPay, Stripe, PayPal 예제

### 인프라
- **컨테이너화**: Docker & Docker Compose
- **모니터링**: Spring Boot Actuator
- **메시지 큐**: Apache Kafka (Spring Kafka)
- **국제화**: 통화 처리를 위한 ICU4J

## 🧪 테스팅

### 테스트 실행
```bash
# 모든 테스트 실행
./gradlew test

# 로컬 프로필로 실행
./gradlew testLocal

# 테스트 리포트 생성
./gradlew test jacocoTestReport
```

### 테스트 커버리지
- **단위 테스트**: 서비스 계층 비즈니스 로직
- **통합 테스트**: API 엔드포인트 및 데이터베이스 작업
- **결제 게이트웨이 테스트**: Stripe/PayPal 모의 및 통합 테스트
- **세금 계산 테스트**: 국가별 세금 규칙 검증

## 📦 배포

### 환경 설정

#### 로컬 개발
```bash
./gradlew bootRunLocal
```

#### 개발 환경
```bash
./gradlew bootRunDev
```

#### 프로덕션 환경
```bash
./gradlew bootRunProd
```

### 환경 변수
```bash
# 데이터베이스
DB_URL=jdbc:mysql://localhost:3306/billing_store
DB_USERNAME=billing_user
DB_PASSWORD=your_secure_password

# 결제 게이트웨이
STRIPE_SECRET_KEY=sk_live_...
STRIPE_PUBLISHABLE_KEY=pk_live_...
PAYPAL_CLIENT_ID=your_paypal_client_id
PAYPAL_CLIENT_SECRET=your_paypal_secret

# Redis (선택사항)
REDIS_HOST=localhost
REDIS_PORT=6379

# Kafka (선택사항)
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## 🔧 설정

### 애플리케이션 프로필

#### 로컬 (`application-local.properties`)
- 로컬 MySQL 데이터베이스 연결
- 디버그 로깅 활성화
- 모의 결제 게이트웨이

#### 개발 (`application-dev.properties`)
- MySQL 데이터베이스 연결
- 상세한 로깅
- 샌드박스 결제 게이트웨이 설정

#### 프로덕션 (`application-prod.properties`)
- 연결 풀이 있는 프로덕션 데이터베이스
- 오류 수준 로깅만
- 라이브 결제 게이트웨이 설정
- 보안 강화

### 코드 표준
- Kotlin 코딩 컨벤션 따르기
- 테스트 커버리지 80% 이상 유지
- API 변경 사항에 대한 문서 업데이트
- 의미 있는 커밋 메시지 사용

## 📋 사용 사례

### SaaS 구독 청구
반복 청구가 필요한 서비스형 소프트웨어 애플리케이션에 적합:
- 월간/연간 구독 플랜
- 무료 체험 기간
- 플랜 업그레이드 및 다운그레이드
- 사용량 기반 청구

### 디지털 제품 판매
디지털 제품 및 서비스 판매에 이상적:
- 전자책, 강의, 소프트웨어 라이센스
- 일회성 서비스 구매
- 국가별 다층 가격 책정
- 즉시 디지털 배송

### 다중 서비스 플랫폼
여러 서비스를 운영하는 조직에 적합:
- 서비스 간 중앙집중식 청구
- 통합 사용자 관리
- 서비스별 제품 카탈로그
- 교차 서비스 분석

### 교육 프로젝트
다음 사항을 배우기에 탁월함:
- 결제 처리 아키텍처
- 구독 청구 시스템
- 다중 테넌트 애플리케이션
- API 설계 및 보안

## 📄 라이센스

이 프로젝트는 MIT 라이센스 하에 라이센스가 부여됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 🙏 참고자료

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Stripe](https://stripe.com)
- [PayPal](https://developer.paypal.com)
- [Kotlin](https://kotlinlang.org)
