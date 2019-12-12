import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessPlugin
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import com.jfrog.bintray.gradle.BintrayPlugin
import io.spring.javaformat.gradle.SpringJavaFormatPlugin
import java.time.LocalDateTime
import nebula.plugin.release.ReleasePlugin

plugins {
    id("nebula.release") apply false
    id("com.jfrog.bintray") apply false
    id("com.diffplug.gradle.spotless") apply false
    id("io.spring.javaformat") apply false
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

    group = "io.mateo"

    repositories {
        jcenter()
    }

    ext["springBootVersion"] = "2.2.1.RELEASE"
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
        plugin<BintrayPlugin>()
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
            }
        }
    }

    configure<BintrayExtension> {
        user = System.getenv("BINTRAY_USER")
        key = System.getenv("BINTRAY_KEY")
        pkg(closureOf<PackageConfig> {
            repo = "maven"
            name = project.name
            setLicenses("Apache-2.0")
            vcsUrl = ""
            setLabels("spring-boot", "twilio")
            publicDownloadNumbers = true
            version(closureOf<VersionConfig> {
                name = project.version.toString()
                desc = "Release - ${project.name} - ${project.version}"
                vcsTag = project.version.toString()
                released = LocalDateTime.now().toString()
            })
            setPublications("mavenJava")
        })
    }
}
