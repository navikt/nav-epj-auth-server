import org.cyclonedx.gradle.CycloneDxTask

plugins {
  kotlin("jvm") version "2.1.0"
  kotlin("plugin.spring") version "2.1.0"
  id("org.springframework.boot") version "3.4.0"
  id("io.spring.dependency-management") version "1.1.7"
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
  implementation("net.logstash.logback:logstash-logback-encoder:8.0")
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

jib {
  container {
   // ports = listOf(System.getenv("port") ?: "9000")
  }
}

tasks {
  withType<Test> {
    useJUnitPlatform {
    }
    testLogging {
      events("skipped", "failed")
      showStackTraces = true
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
  }

  withType<CycloneDxTask>{
    setIncludeConfigs(listOf("runtimeClasspath", "compileClasspath"))
  }

}