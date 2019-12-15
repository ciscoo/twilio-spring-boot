plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
}

repositories {
    jcenter()
    mavenCentral()
}

extra["gsonVersion"] = "2.8.6"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.google.code.gson:gson:${property("gsonVersion")}")
}

gradlePlugin {
    plugins {
        val projectReleasePlugin by plugins.creating {
            id = "io.mateo.release-plugin"
            implementationClass = "io.mateo.gradle.ProjectReleasePlugin"
        }
    }
}
