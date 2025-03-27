val ktorVersion = "2.3.7"

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

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}