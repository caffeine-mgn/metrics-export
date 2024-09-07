buildscript {
    val kotlinVersion = properties.get("kotlin.version") as String
    repositories {
        mavenLocal()
        mavenCentral()
        maven(url = "https://maven.google.com")
        gradlePluginPortal()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

val kotlinVersion = project.property("kotlin.version") as String
val kotlinxCoroutinesVersion = project.property("kotlinx_coroutines.version") as String
val kotlinxSerializationVersion = project.property("kotlinx_serialization.version") as String

buildConfig {
    packageName(project.group.toString())
    buildConfigField("String", "KOTLIN_VERSION", "\"$kotlinVersion\"")
    buildConfigField("String", "KOTLINX_COROUTINES_VERSION", "\"$kotlinxCoroutinesVersion\"")
    buildConfigField("String", "KOTLINX_SERIALIZATION_VERSION", "\"$kotlinxSerializationVersion\"")
}

plugins {
    kotlin("jvm") version "1.8.10"
    id("com.github.gmazzo.buildconfig") version "3.0.3"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://repo.binom.pw")
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://maven.google.com")
    gradlePluginPortal()
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")
    api("org.jetbrains.kotlin:kotlin-noarg:$kotlinVersion")
//    api("pw.binom.static-css:plugin:0.1.32")
    api("com.bmuschko:gradle-docker-plugin:6.4.0")
//    api("pw.binom:binom-publish:0.1.6")
}
