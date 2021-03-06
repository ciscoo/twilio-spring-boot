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
                url = if (project.version.toString().endsWith("SNAPSHOT")) {
                    uri("https://oss.sonatype.org/content/repositories/snapshots")
                } else {
                    uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                }
                credentials {
                    username = System.getenv("OSSRH_USER_TOKEN")
                    password = System.getenv("OSSRH_PWD_TOKEN")
                }
            }
        }
    }

    group = "io.mateo.spring"

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
    apply {
        plugin<SigningPlugin>()
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
        withJavadocJar()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                pom {
                    val mavenPom = this
                    afterEvaluate {
                        mavenPom.name.set(description)
                        mavenPom.description.set(description)
                    }
                    url.set("https://github.com/ciscoo/twilio-spring-boot")
                    licenses {
                        license {
                            name.set("Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            name.set("Francisco Mateo")
                            email.set("cisco21c@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/twilio-spring-boot.git")
                        developerConnection.set("scm:git:ssh://github.com/twilio-spring-boot.git")
                        url.set("https://github.com/ciscoo/twilio-spring-boot")
                    }
                    issueManagement {
                        system.set("GitHub")
                        url.set("https://github.com/ciscoo/twilio-spring-boot/issues")
                    }
                }
                versionMapping {
                    usage("java-api") {
                        fromResolutionResult()
                    }
                    usage("java-runtime") {
                        fromResolutionResult()
                    }
                }
            }
        }
    }

    // https://discuss.gradle.org/t/unable-to-publish-artifact-to-mavencentral/33727/3
    tasks.withType<GenerateModuleMetadata> {
        enabled = false
    }

    configure<SigningExtension> {
        isRequired = !project.version.toString().endsWith("SNAPSHOT")
        sign(project.extensions.getByType(PublishingExtension::class.java).publications["mavenJava"])
    }
}
