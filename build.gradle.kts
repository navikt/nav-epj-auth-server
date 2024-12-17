plugins {
  kotlin("jvm") version "2.0.21"
  kotlin("plugin.spring") version "2.1.0"
  id("org.springframework.boot") version "3.4.0"
  id("io.spring.dependency-management") version "1.1.6"
  id ("com.google.cloud.tools.jib") version "3.4.4"
  id ("org.cyclonedx.bom") version "1.10.0"
}


group = "no.nav.helse"
version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation("org.springframework.boot:spring-boot-starter-actuator")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
tasks.cyclonedxBom {
  setIncludeConfigs(listOf("runtimeClasspath", "compileClasspath"))
}
