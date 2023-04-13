plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.12.0"
}

group = "com.minivv"
version = "1.0.23.0312"

repositories {
    maven { url = uri("https://nexus.bsdn.org/content/groups/public/") }
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}

dependencies {
    implementation("com.theokanning.openai-gpt3-java:service:0.11.0")
    implementation("uk.com.robust-it:cloning:1.9.12")
}


// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2021.1.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf(/* Plugin Dependencies */))
}


tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    patchPluginXml {
        sinceBuild.set("211")
        untilBuild.set("231.*")
    }

    signPlugin {
        certificateChain.set(System.getenv("perm:a3V3ZWlndWdl.OTItNzY0OQ==.IZBt3rSpdcHj98D0e7FhxaeesCX6uR"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("kuweiguge"))
    }
}
