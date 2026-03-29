plugins {
    java
    id("com.gradleup.shadow") version "8.3.0"
}

group = "tf.tuff"
version = "1.2.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven("https://repo.grim.ac/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.viaversion.com/everything/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly(libs.paper.api)

    implementation(libs.packetevents)

    compileOnly(libs.viaversion)
    compileOnly(libs.viabackwards)

    compileOnly(libs.fastutil)

    implementation(libs.grimapi)
    implementation(libs.jackson.databind)
    implementation(libs.java.websocket)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(
            mapOf(
                "version" to project.version,
                "name" to project.name
            )
        )
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("TuffX.jar")

    relocate("com.github.retrooper.packetevents", "tf.tuff.packetevents")
    relocate("io.github.retrooper.packetevents", "tf.tuff.packetevents")
    relocate("com.fasterxml.jackson", "tf.tuff.jackson")
    relocate("org.java_websocket", "tf.tuff.websocket")

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("META-INF/LICENSE")
    exclude("META-INF/NOTICE")
    exclude("META-INF/versions/**")
    exclude("module-info.class")
}

tasks.jar {
    enabled = false
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
