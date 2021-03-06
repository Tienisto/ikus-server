import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
	id("org.springframework.boot") version "2.4.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.4.31"
	kotlin("plugin.spring") version "1.4.31"
}

group = "de.ovgu"
version = "1.8.0"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {

	// core
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")

	// database
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.flywaydb:flyway-core")
	runtimeOnly("io.r2dbc:r2dbc-postgresql")
	runtimeOnly("org.postgresql:postgresql")

	// jwt
	implementation("io.jsonwebtoken:jjwt-api:0.11.2")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.2")

	// tools
	implementation("org.jsoup:jsoup:1.13.1") // parse html
	implementation("com.drewnoakes:metadata-extractor:2.14.0") // get image orientation

	// test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")

	// kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

task<Copy>("updateVue") {
	from("src/main/vue/dist", "src/main/vue/dist/index.html")
	into("$buildDir/resources/main/static")
}

tasks.withType<ProcessResources> {
	dependsOn("updateVue")
}

// add build.properties, so the backend can read its version at runtime
tasks.withType<ProcessResources> {
	doLast {
		File("$buildDir/resources/main/build.properties").writeText("""
            version=${project.version}
            date=${LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}
        """.trimIndent())
	}
}