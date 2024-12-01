import edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask
import io.github.peppshabender.r4j.gradle.utils.r4j

plugins {
    id("java")
    id("application")
    id("com.diffplug.spotless") version "6.25.0"
    id("edu.sc.seis.launch4j") version "3.0.6"
    id("io.github.peppshabender.r4j") version "0.0.2"
}

group = "de.peppshabender.deskterminal"
version = "0.0.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies/")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

val mainClassPath: String by extra("de.peppshabender.deskterminal.Deskterminal")

application {
    mainClass.set(mainClassPath)
}

tasks.withType<DefaultLaunch4jTask> {
    mainClassName = mainClassPath
}

val lombok: String by extra("1.18.34")
val jediterm: String by extra("3.47")

dependencies {
    annotationProcessor(libs.lombok)
    compileOnly(libs.lombok)

    implementation(libs.jediterm.ui)
    implementation(libs.jediterm.core)
    implementation(libs.jediterm.pty)
    implementation(libs.pty4j)
    implementation(libs.jna)
    implementation(libs.darklaf.core)
    implementation(r4j.java)
}

spotless {
    java {
        palantirJavaFormat("2.39.0").formatJavadoc(true)
    }
}