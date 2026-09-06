pluginManagement {
    includeBuild("build-logic")
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
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CryptoTrack"

include(":app")
include(":core:common")
include(":core:model")
include(":core:network")
include(":core:data")
include(":core:datastore")
include(":core:designsystem")
include(":core:navigation")
include(":feature:market")
include(":feature:coin-details")
include(":feature:favorites")
include(":feature:search")
include(":feature:portfolio")
