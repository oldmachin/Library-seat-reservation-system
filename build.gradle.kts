plugins {
    id("java")
    id("org.springframework.boot") version "4.0.0"
}

group = "com.anonymous"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.0"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:4.0.0")
    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("org.springframework.boot:spring-boot-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-websocket")


    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.0")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:4.0.0")
    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation ("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly ("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly ("io.jsonwebtoken:jjwt-jackson:0.13.0")
}

tasks.test {
    useJUnitPlatform()
}