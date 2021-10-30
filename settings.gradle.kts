rootProject.name = "Athena"

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
    }
}