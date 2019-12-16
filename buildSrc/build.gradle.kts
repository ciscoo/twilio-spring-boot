import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
}

repositories {
    jcenter()
    mavenCentral()
}

extra["gsonVersion"] = "2.8.6"
extra["ktorVersion"] = "1.2.6"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.ktor:ktor-client-core:${property("ktorVersion")}")
    implementation("io.ktor:ktor-client-apache:${property("ktorVersion")}")
    implementation("io.ktor:ktor-client-json-jvm:${property("ktorVersion")}")
    implementation("io.ktor:ktor-http-jvm:${property("ktorVersion")}")
    implementation("io.ktor:ktor-client-gson:${property("ktorVersion")}")
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

tasks.named("compileKotlin", KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
