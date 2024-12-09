import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.3.6"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "de.ovgu"
version = "1.10.2"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// core
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")

	// database
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework:spring-jdbc")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")

	// jwt
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// tools
	implementation("org.jsoup:jsoup:1.18.3") // parse html
	implementation("com.drewnoakes:metadata-extractor:2.18.0") // get image orientation
	implementation("com.github.librepdf:openpdf:1.3.28") // generate pdf
	implementation("org.apache.poi:poi-ooxml:5.3.0") // generate word document

	// kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

task<Copy>("updateVue") {
	from("src/main/vue/dist")
	into("${layout.buildDirectory.get()}/resources/main/static")
}

tasks.withType<ProcessResources> {
	dependsOn("updateVue")
}

// add build.properties, so the backend can read its version at runtime
tasks.withType<ProcessResources> {
	doLast {
		File("${layout.buildDirectory.get()}/resources/main/build.properties").writeText("""
            version=${project.version}
            date=${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
        """.trimIndent())
	}
}
