import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 Should be one of the following depending on your operating system or the platform you are targeting

 x64 Windows - "windows"
 x86 Windows - "windows-x86"
 arm64 Windows - "windows-arm64"

 x64 Linux - "linux"
 arm 64 Linux - "linux-arm64"
 arm 32 Linux - "linux-arm32"
 */
val osString = "windows"
val lwjglNatives = "natives-${osString}"

//this can be any valid tag on this repo, or a short commit id if you are using Jitpack.
//More info at https://jitpack.io/.
val enignetsVersion = "1.0.1a_5"

val lwjglVersion = "3.2.3"
val jomlVersion = "1.10.1"

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
}

group = "c1fr1"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")

    maven {
        url = uri("https://maven.pkg.github.com/c1fr1/Enignets")
        credentials {
            username = project.property("githubUsername").toString()
            password = project.property("githubPAT").toString()
        }
    }
}



dependencies {
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.joml", "joml", jomlVersion)

    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)

    implementation("c1fr1:enignets:${enignetsVersion}")//using specific version on github packages
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>() {
    manifest {
        attributes(mapOf("Main-Class" to "MainKt"))
    }
    archiveFileName.set("Infector-Automata-$osString.jar")
}

application {
    mainClass.set("MainKt")
}