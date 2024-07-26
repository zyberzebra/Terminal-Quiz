// build.gradle.kts (kotlin script)
plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.varabyte.kotter:kotter-jvm:1.1.2")
    testImplementation("com.varabyte.kotterx:kotter-test-support-jvm:1.1.2")
}