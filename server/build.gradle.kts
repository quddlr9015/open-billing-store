plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "io"
version = "0.0.1-SNAPSHOT"
description = "A Kotlin Spring Boot server for handling billing and store operations"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.springframework.kafka:spring-kafka")
	runtimeOnly("com.mysql:mysql-connector-j")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Environment-specific build configurations
tasks.register("bootRunLocal") {
	group = "application"
	description = "Run the application with local profile"
	doFirst {
		tasks.bootRun.configure {
			systemProperty("spring.profiles.active", "local")
		}
	}
	finalizedBy(tasks.bootRun)
}

tasks.register("bootRunDev") {
	group = "application"
	description = "Run the application with dev profile"
	doFirst {
		tasks.bootRun.configure {
			systemProperty("spring.profiles.active", "dev")
		}
	}
	finalizedBy(tasks.bootRun)
}

tasks.register("bootRunProd") {
	group = "application"
	description = "Run the application with prod profile"
	doFirst {
		tasks.bootRun.configure {
			systemProperty("spring.profiles.active", "prod")
		}
	}
	finalizedBy(tasks.bootRun)
}

tasks.register("testLocal") {
	group = "verification"
	description = "Run tests with local profile"
	doFirst {
		tasks.test.configure {
			systemProperty("spring.profiles.active", "local")
		}
	}
	finalizedBy(tasks.test)
}
