@file:Suppress("UnstableApiUsage")

import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import io.spring.javaformat.gradle.SpringJavaFormatPlugin
import nebula.plugin.release.ReleasePlugin

plugins {
    id("nebula.release") apply false
    id("com.diffplug.gradle.spotless") apply false
    id("io.spring.javaformat") apply false
    id("io.mateo.release-plugin")
}

val license = file("src/spotless/apache-license-2.0.java")

allprojects {
    apply {
        plugin<JavaLibraryPlugin>()
        plugin<MavenPublishPlugin>()
        plugin<ReleasePlugin>()
        plugin<SpotlessPlugin>()
        plugin<SpringJavaFormatPlugin>()
    }

    configure<SpotlessExtension> {
        java {
            licenseHeaderFile(license)
        }
        kotlinGradle {
            ktlint()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    configure<JavaPluginConvention> {
        targetCompatibility = JavaVersion.VERSION_1_8
        sourceCompatibility = JavaVersion.VERSION_1_8
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                url = uri(property("publicationRepo") as String)
                credentials {
                    username = System.getenv("BINTRAY_USER")
                    password = System.getenv("BINTRAY_KEY")
                }
            }
        }
    }

    group = "io.mateo"

    repositories {
        jcenter()
    }

    ext["springBootVersion"] = "2.2.2.RELEASE"
    ext["twilioVersion"] = "7.42.0"

    dependencies {
        "implementation"(platform("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}"))
        "annotationProcessor"(platform("org.springframework.boot:spring-boot-dependencies:${property("springBootVersion")}"))
        "testImplementation"("org.springframework.boot:spring-boot-starter-test") {
            exclude("org.junit.vintage", "junit-vintage-engine")
        }
    }

    tasks.withType(Test::class.java) {
        useJUnitPlatform()
    }
}

subprojects {
    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }
}
