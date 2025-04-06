val ktorVersion = "2.3.7"
val exposedVersion = "0.45.0"
val hikaricpVersion = "5.0.1"
val mysqlVersion = "8.0.33"

plugins {
    kotlin("jvm") version "2.0.21"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.example.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // 가장 기본적인 ktor 의존성 주입
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")

    // yaml 설정 파일을 위한 의존성
    implementation("io.ktor:ktor-server-config-yaml-jvm:$ktorVersion")

    // ktor에서 slf4j를 요구하기 떄문에, 설치
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Content Negotiation 기능을 위한 의존성
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")

    // Jackson Java Time 모듈 (LocalDateTime 처리)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Exposed 라이브러리 (Kotlin SQL 프레임워크)
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // MySQL 드라이버
    implementation("mysql:mysql-connector-java:$mysqlVersion")

    // Connection Pool
    implementation("com.zaxxer:HikariCP:$hikaricpVersion")

    // 의존성 관리
    implementation("io.insert-koin:koin-ktor:3.5.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.0")

    // auth 검증 커스텀
    implementation("io.ktor:ktor-server-auth:$ktorVersion")

    // PASETO 의존성
    implementation("dev.paseto:jpaseto-api:0.7.0")
    implementation("dev.paseto:jpaseto-impl:0.7.0")
    implementation("dev.paseto:jpaseto-jackson:0.7.0")

    // minIO
    implementation("io.minio:minio:8.5.5")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}