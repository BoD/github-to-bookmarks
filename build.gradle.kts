import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFileInstruction

plugins {
  alias(libs.plugins.kotlin.jvm)
  id("application")
  alias(libs.plugins.apollo)
  alias(libs.plugins.dockerJavaApplication)
}

group = "org.jraf"
version = "1.0.0"

kotlin {
  jvmToolchain(25)
}

application {
  mainClass.set("org.jraf.githubtobookmarks.main.MainKt")
}

apollo {
  service("github") {
    packageName.set("org.jraf.githubtobookmarks")

    introspection {
      endpointUrl.set("https://api.github.com/graphql")
      schemaFile.set(file("src/main/graphql/schema.graphqls"))
      headers.put("Authorization", "Bearer ${project.findProperty("githubOauthKey")}")
    }
  }
}

dependencies {
  // Ktor
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.ktor.server.defaultHeaders)
  implementation(libs.ktor.server.statusPages)

  implementation(libs.slf4j.simple)

  // JSON
  implementation(libs.kotlinx.serialization.json)

  // Apollo
  implementation(libs.apollo.runtime)
}

docker {
  javaApplication {
    baseImage.set("eclipse-temurin:25")
    maintainer.set("BoD <BoD@JRAF.org>")
    ports.set(listOf(8080))
    images.add("bodlulu/${rootProject.name.lowercase()}:latest")
    jvmArgs.set(listOf("-Xms16m", "-Xmx128m"))
  }
  registryCredentials {
    username.set(System.getenv("DOCKER_USERNAME"))
    password.set(System.getenv("DOCKER_PASSWORD"))
  }
}

tasks.withType<DockerBuildImage> {
  platform.set("linux/amd64")
}

tasks.withType<Dockerfile> {
  // Install curl
  runCommand("apt-get update")
  runCommand("apt-get install -y curl")

  // Download the OpenTelemetry Java agent
  runCommand("curl -L -O https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar")

  // OpenTelemetry Java agent configuration
  environmentVariable(
    mapOf(
      "JAVA_TOOL_OPTIONS" to "-javaagent:opentelemetry-javaagent.jar",
      "OTEL_LOGS_EXPORTER" to "none",
      "OTEL_METRICS_EXPORTER" to "none",
      "OTEL_TRACES_EXPORTER" to "otlp",
      "OTEL_SERVICE_NAME" to rootProject.name.lowercase(),
    )
  )

  // Move the COPY instructions to the end
  // See https://github.com/bmuschko/gradle-docker-plugin/issues/1093
  instructions.set(
    instructions.get().sortedBy { instruction ->
      if (instruction.keyword == CopyFileInstruction.KEYWORD) 1 else 0
    }
  )
}

// `./gradlew downloadGithubApolloSchemaFromIntrospection` to download the schema
// `./gradlew distZip` to create a zip distribution
// `./gradlew refreshVersions` to update dependencies
// `DOCKER_USERNAME=<your docker hub login> DOCKER_PASSWORD=<your docker hub password> ./gradlew dockerPushImage` to build and push the image
