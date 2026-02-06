plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.10"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "com.ziondev"
version = findProperty("pluginVersion")?.toString() ?: "0.0.0-dev"

val envFile = file(".env")
if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        val parts = line.split("=", limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim()
            val value = parts[1].trim()
            if (key.isNotEmpty()) {
                System.setProperty(key, value)
            }
        }
    }
}

fun getEnvOrProperty(key: String): String? = System.getenv(key) ?: System.getProperty(key)

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("io.mockk:mockk:1.13.8")
}

intellij {
    version.set("2023.2.5")
    type.set("IC")
}
    
tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    test {
        useJUnitPlatform()
    }
    
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("253.*")
    }

    signPlugin {
        certificateChain.set(getEnvOrProperty("CERTIFICATE_CHAIN"))
        privateKey.set(getEnvOrProperty("PRIVATE_KEY"))
        password.set(getEnvOrProperty("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(getEnvOrProperty("PUBLISH_TOKEN"))
    }
}