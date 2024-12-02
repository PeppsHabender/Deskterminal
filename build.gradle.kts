import edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask
import io.github.peppshabender.r4j.gradle.utils.r4j

plugins {
    id("java")
    id("application")
    id("com.diffplug.spotless") version "6.25.0"
    id("edu.sc.seis.launch4j") version "3.0.6"
    id("io.github.peppshabender.r4j") version "0.0.2"
}

///////// Constants

group = "de.peppshabender.deskterminal"
version = "0.0.1"
val distGroup = "dist"
val distDir = "distributions"
val appName = "Deskterminal"
val mainClassPath = "$group.$appName"

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

application {
    mainClass.set(mainClassPath)
}

val copyJre = tasks.register<Copy>("copyJre") {
    from(projectDir.resolve("lib/jdk-17.0.13+11-jre"))
    into(layout.buildDirectory.dir("$appName/jre"))
}

val launch4j = tasks.withType<DefaultLaunch4jTask> {
    mainClassName = mainClassPath
    bundledJrePath = "jre"
    outputDir = appName

    dependsOn(copyJre)
}

tasks.register<Zip>("zipBundledDist") {
    group = distGroup

    from(layout.buildDirectory.dir(appName))

    archiveBaseName = "desktermial_bundled"
    destinationDirectory.set(layout.buildDirectory.dir(distDir))

    dependsOn(launch4j)
}

tasks.register<Zip>("zipDist") {
    group = distGroup

    from(layout.buildDirectory.dir(appName))

    exclude("jre")
    archiveBaseName = "deskterminal_raw"
    destinationDirectory.set(layout.buildDirectory.dir(distDir))

    dependsOn(launch4j)
}

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