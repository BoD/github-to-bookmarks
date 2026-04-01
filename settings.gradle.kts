rootProject.name = "github-to-bookmarks"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
    mavenLocal()
  }
}

plugins {
    // See https://splitties.github.io/refreshVersions/
    id("de.fayard.refreshVersions") version "0.60.5"
}

