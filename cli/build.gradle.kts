plugins {
  kotlin("jvm") version "2.3.0"
  kotlin("plugin.spring") version "2.3.0"
  id("org.springframework.boot") version "4.0.5"
  id("io.spring.dependency-management") version "1.1.7"
  application
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("reflect"))
  implementation("org.springframework.boot:spring-boot-starter")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.data:spring-data-commons")
  implementation("org.springframework.shell:spring-shell-starter:4.0.1")
  implementation("org.springframework.shell:spring-shell-jline:4.0.1")
  implementation("git.walhay:modweave")
  implementation("git.walhay:ui")
  implementation("git.walhay:business-logic")
  implementation("git.walhay:data-access")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
  jvmToolchain(25)
}

application {
  mainClass.set("git.walhay.modweave.cli.MainKt")
}

tasks.named<JavaExec>("run") {
  standardInput = System.`in`
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
  standardInput = System.`in`
}
