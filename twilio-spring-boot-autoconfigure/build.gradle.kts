plugins {
    jacoco
}

dependencies {
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    api("com.twilio.sdk:twilio:${property("twilioVersion")}")
}
