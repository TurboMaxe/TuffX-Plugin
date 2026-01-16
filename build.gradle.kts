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
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.viaversion.com/everything/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.dmulloy2.net/repository/public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")

    implementation("com.github.retrooper:packetevents-spigot:2.11.1")

    compileOnly("com.viaversion:viabackwards:5.3.2")
    compileOnly("com.viaversion:viaversion:5.4.1")
    

    compileOnly("it.unimi.dsi:fastutil:8.5.16")
    implementation("ac.grim.grimac:GrimAPI:1.2.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    implementation("org.java-websocket:Java-WebSocket:1.5.4")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
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
