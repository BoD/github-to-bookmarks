import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version "1.5.31"
    id("application")
    id("com.github.ben-manes.versions").version("0.39.0")
    id("com.apollographql.apollo3").version("3.0.0")
}

group = "org.jraf"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

val versions = mapOf(
    "gradle" to "7.2",
    "ktor" to "1.6.4",
    "logback" to "1.2.6",
    "json" to "20210307",
    "apollo" to "3.0.0"
)

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = versions["gradle"]
    }
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        candidate.version.contains("alpha", true)
    }
}

tasks.register("stage") {
    dependsOn(":installDist")
}

application {
    mainClassName = "org.jraf.githubtobookmarks.main.MainKt"
}

apollo {
    packageName.set("org.jraf.githubtobookmarks")
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core:${versions["ktor"]}")
    implementation("io.ktor:ktor-server-netty:${versions["ktor"]}")

    // Logback
    runtimeOnly("ch.qos.logback:logback-classic:${versions["logback"]}")

    // JSON
    implementation("org.json:json:${versions["json"]}")

    // Apollo
    implementation("com.apollographql.apollo3:apollo-runtime:${versions["apollo"]}")
}

// Run `./gradlew distZip` to create a zip distribution
