import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version: String by project
val logback_version: String by project
val jackson_version: String by project

plugins {
    application
    kotlin("jvm") version "1.3.0-rc-131"
}

group = "rogue"
version = "1.0-SNAPSHOT"

dependencies {
    compile(kotlin("stdlib-jdk8"))

    compile("io.ktor:ktor-server-core:$ktor_version")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("ch.qos.logback:logback-classic:$logback_version")
    compile("io.ktor:ktor-jackson:$ktor_version")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")

    testCompile("junit:junit:4.12")
}

repositories {
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "io.ktor.server.netty.DevelopmentEngine"
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}
