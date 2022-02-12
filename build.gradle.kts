plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.0"
    application
}

var athenaVersion = "3.0.0_${System.getenv("BUILD_NUMBER") ?: System.getProperty("BUILD_NUMBER") ?: "DEV"}"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")

    implementation("org.litote.kmongo:kmongo:4.3.0")

    implementation("org.slf4j:slf4j-nop:1.7.32")
    implementation("io.javalin:javalin:4.0.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("org.simplejavamail:simple-java-mail:6.6.1")
}

application {
    mainClass.set("com.wynntils.athena.AthenaKt")
}

tasks {
    shadowJar {
        archiveFileName.set("WynntilsAthena-${athenaVersion}.jar")
    }
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}