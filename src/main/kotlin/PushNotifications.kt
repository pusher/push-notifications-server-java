package com.pusher

import jdk.incubator.http.HttpClient
import jdk.incubator.http.HttpRequest
import jdk.incubator.http.HttpResponse

import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class PushNotifications(private val instanceId: String, private val secretKey: String) {

    private var interestsMaxLength = 164

    init {
        if (instanceId.isEmpty()) {
            throw IllegalArgumentException("instanceId can't be an empty string")
        }

        if (secretKey.isEmpty()) {
            throw IllegalArgumentException("secretKey can't be an empty string")
        }
    }

    @Throws(IOException::class, InterruptedException::class, URISyntaxException::class)
    fun publish(payload: String) {
        if (payload.isEmpty()) {
            throw IllegalArgumentException("payload should not be an empty string")
        }

        if (payload.length > interestsMaxLength) {
            throw IllegalArgumentException(String.format("interest %s is longer than the maximum of %d characters", payload, interestsMaxLength))
        }

        val client = HttpClient.newHttpClient()
        val url = String.format("http://%s.pushnotifications.pusher.com/publish_api/v1/instances/%s/publishes", this.instanceId, this.instanceId)

        val request = HttpRequest.newBuilder()
                .uri(URI(url))
                .setHeader("Accept", "application/json")
                .setHeader("Content-Type", "application/json")
                .setHeader("Authorization", String.format("Bearer %s", this.secretKey))
                .POST(HttpRequest.BodyProcessor.fromString(payload))
                .build()

        val response = client.send(request, HttpResponse.BodyHandler.discard<String>(null))
        System.out.printf("Received HTTP Response Code: %d%n", response.statusCode())
    }
}
