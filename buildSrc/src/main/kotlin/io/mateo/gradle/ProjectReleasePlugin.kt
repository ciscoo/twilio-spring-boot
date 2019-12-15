package io.mateo.gradle

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

data class ClientPayload(
        @SerializedName("release_type") val releaseType: String,
        @SerializedName("release_scope") val releaseScope: String
)

data class DispatchEvent(
        @SerializedName("event_type") val eventType: String,
        @SerializedName("client_payload") val clientPayload: ClientPayload
)

class ProjectReleasePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("projectRelease") {
            it.doLast {
                var releaseType = project.findProperty("releaseType") as String?
                var releaseScope = project.findProperty("releaseScope") as String?

                if (releaseType == null) {
                    releaseType = "snapshot"
                }

                if (releaseScope == null) {
                    releaseScope = "minor"
                }

                val requestBody = DispatchEvent("release-dispatch", ClientPayload(releaseType, releaseScope))
                val json = Gson().toJson(requestBody)

                val client = HttpClient.newHttpClient()
                val request = HttpRequest.newBuilder()
                        .uri(URI.create(project.property("dispatchURI") as String))
                        .header("Authorization", "token ${System.getenv("TOKEN")}")
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("Accept", "application/vnd.github.everest-preview+json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build()

                val response = client.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() in 400..599) {
                    throw GradleException("Error dispatching release event: ${response.body()}")
                }

                println("Successfully dispatched release event: $json")
            }
        }
    }
}
