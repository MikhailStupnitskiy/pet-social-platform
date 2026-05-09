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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Pet Social Platform"
include(":app")
include(":core-common")
include(":core-auth")
include(":core-ui")
include(":core-datastore")
include(":core-designsystem")
include(":core-navigation")
include(":core-network")
include(":feature-auth")
include(":feature-chat")
include(":feature-pets")
include(":feature-profile")
include(":feature-routine")
include(":feature-matching")
include(":feature-feed")
