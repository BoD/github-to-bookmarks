import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    kotlin("jvm")
    id("application")
    id("com.apollographql.apollo3")
    id("com.bmuschko.docker-java-application")
}

group = "org.jraf"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

application {
    mainClass.set("org.jraf.githubtobookmarks.main.MainKt")
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

docker {
    javaApplication {
        maintainer.set("BoD <BoD@JRAF.org>")
        ports.set(listOf(8080))
        images.add("bodlulu/${rootProject.name}:latest")
    }
    registryCredentials {
        username.set(System.getenv("DOCKER_USERNAME"))
        password.set(System.getenv("DOCKER_PASSWORD"))
    }
}

tasks.withType<DockerBuildImage> {
    platform.set("linux/amd64")
}

// `./gradlew distZip` to create a zip distribution
// `./gradlew refreshVersions` to update dependencies
// `DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage` to build and push the image
