plugins {
    kotlin("jvm")
    id("application")
    id("com.apollographql.apollo3")
}

group = "org.jraf"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

application {
    mainClassName = "org.jraf.githubtobookmarks.main.MainKt"
}

apollo {
    packageName.set("org.jraf.githubtobookmarks")
}

dependencies {
    // Ktor
    implementation(Ktor.server.core)
    implementation(Ktor.server.netty)
    implementation(Ktor.server.defaultHeaders)
    implementation(Ktor.server.statusPages)

    // Logback
    runtimeOnly("ch.qos.logback:logback-classic:_")

    // JSON
    implementation(KotlinX.serialization.json)

    // Apollo
    implementation(ApolloGraphQL.runtime)
}

// `./gradlew distZip` to create a zip distribution
// `./gradlew refreshVersions` to update dependencies
