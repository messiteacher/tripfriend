plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("kapt") version "1.9.25"  // kapt 플러그인 추가
	id("org.springframework.boot") version "3.2.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.tripfriend"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

repositories {
	mavenCentral()
}

val queryDslVersion = "5.0.0"

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")

	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")

	// Development tools
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Database
	runtimeOnly("com.h2database:h2")
	runtimeOnly("com.mysql:mysql-connector-j")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testImplementation("io.mockk:mockk:1.13.17")

	// (선택) Spring Test 포함 시
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.mockito") // mockito 제외 가능 (MockK 사용 시)
	}

	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

	// Email
	implementation("org.springframework.boot:spring-boot-starter-mail")

	// Environment variables
	implementation("me.paulschwarz:spring-dotenv:3.0.0")

	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// QueryDSL - annotationProcessor를 kapt로 변경
	implementation("com.querydsl:querydsl-jpa:${queryDslVersion}:jakarta")
	kapt("com.querydsl:querydsl-apt:${queryDslVersion}:jakarta")
	kapt("jakarta.annotation:jakarta.annotation-api")
	kapt("jakarta.persistence:jakarta.persistence-api")

	// OAuth2
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

	// Redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation(kotlin("stdlib-jdk8"))
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}

	// 필요한 경우 sourceSets 설정
	sourceSets.main {
		kotlin.srcDir("src/main/kotlin")
		kotlin.srcDir("src/main/java") // 자바 디렉토리에 있는 코틀린 파일도 컴파일
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// QueryDSL 설정
val querydslDir = "$buildDir/generated/querydsl"

// kapt 설정 (코틀린을 위한 어노테이션 프로세싱)
kapt {
	arguments {
		arg("querydsl.generatedAnnotationClass", "javax.annotation.Generated")
	}
	correctErrorTypes = true
	javacOptions {
		option("Xmaxerrs", 2000)
	}
	keepJavacAnnotationProcessors = true
}

// sourceSets 설정 (자바와 코틀린 소스를 모두 포함)
sourceSets {
	main {
		java.srcDir(querydslDir)
	}
}

// 생성된 QClass 파일을 clean 태스크에 포함
tasks.named("clean") {
	doLast {
		file(querydslDir).deleteRecursively()
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
	annotation("org.springframework.data.annotation.CreatedDate")
	annotation("org.springframework.data.annotation.LastModifiedDate")
	// 필요한 경우 더 많은 애노테이션 추가
}