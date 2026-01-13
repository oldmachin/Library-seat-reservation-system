plugins {
    id("java")
    id("org.springframework.boot") version "3.3.7"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.anonymous"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.mybatis:mybatis-spring:3.0.3")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-jdbc
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // Source: https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.7")

    // Source: https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    runtimeOnly("com.mysql:mysql-connector-j")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-amqp
    implementation("org.springframework.boot:spring-boot-starter-amqp")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}