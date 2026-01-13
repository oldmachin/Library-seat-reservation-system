plugins {
    id("java")
}

group = "com.anonymous"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-web
    implementation("org.springframework.boot:spring-boot-starter-web:4.0.0")

    // Source: https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter
    implementation("com.baomidou:mybatis-plus-boot-starter:3.5.5")

    // Source: https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    runtimeOnly("com.mysql:mysql-connector-j:9.5.0")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis:4.0.0")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-amqp
    implementation("org.springframework.boot:spring-boot-starter-amqp:4.0.0")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket:4.0.0")

    // Source: https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-test
    testImplementation("org.springframework.boot:spring-boot-starter-test:4.0.0")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}