pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Hasbi"

// App
include(":app")

// Core modules
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-design")
include(":core:core-data")
include(":core:core-domain")
include(":core:core-util")

// Feature modules
include(":feature-onboarding")
include(":feature-dashboard")
include(":feature-transactions")
include(":feature-wallets")
include(":feature-budgets")
include(":feature-debts")
include(":feature-settings")
include(":feature-backup")
