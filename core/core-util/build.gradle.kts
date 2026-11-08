plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Pure Kotlin JVM module — no Android dependencies
// MoneyFormatter, DateTimeUtils, Result, HasbiError all use only java.* stdlib
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // Pure Kotlin coroutines (NOT android variant)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.property)
}
