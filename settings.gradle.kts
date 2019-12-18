pluginManagement {
    plugins {
        id("nebula.release") version "13.2.1"
        id("com.jfrog.bintray") version "1.8.4"
        id("com.diffplug.gradle.spotless") version "3.26.1"
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "io.spring.javaformat") {
                useModule("io.spring.javaformat:spring-javaformat-gradle-plugin:0.0.15")
            }
        }
    }
}

rootProject.name = "twilio-spring-boot"

include("twilio-spring-boot-starter")
include("twilio-spring-boot-autoconfigure")
