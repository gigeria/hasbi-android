plugins {
    alias(libs.plugins.kotlin.jvm)
}

// Pure Kotlin — no Android dependencies
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":core:core-util"))
    implementation(project(":core:core-database"))  // For entity access (BudgetCycleEntity, etc.)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.kotest.runner)
    testImplementation(libs.kotest.property)
}
