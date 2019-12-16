package io.mateo.gradle

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.HttpMethod
import kotlinx.coroutines.*

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

                runBlocking {
                    val statusCode = HttpClient(Apache).use { client ->
                        client.post<HttpStatusCode> {
                            url(project.property("dispatchURI") as String)
                            method = HttpMethod.Post
                            body = Gson().toJson(DispatchEvent("release-dispatch", ClientPayload(releaseType, releaseScope)))
                            headers {
                                append("Authorization", "token ${System.getenv("TOKEN")}")
                                appendAll("Accept", listOf(
                                        "7application/vnd.github.v3+json",
                                        "application/vnd.github.everest-preview+json"
                                ))
                            }
                        }
                    }

                    if (statusCode.value in 400..599) {
                        throw GradleException("Error dispatching release event: ${statusCode.description}")
                    }

                    println("Successfully dispatched release event.")
                }
            }
        }
    }
}
