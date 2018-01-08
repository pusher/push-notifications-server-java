package com.pusher

import java.io.IOException
import java.net.URISyntaxException
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients


class PushNotifications(private val instanceId: String, private val secretKey: String) {

    private val interestsMaxLength = 164
    private val baseURL = "https://$instanceId.pushnotifications.pusher.com/publish_api/v1"

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

        var client = HttpClients.createDefault()
        val url = String.format("$baseURL/instances/%s/publishes", this.instanceId)
        var httpPost = HttpPost(url)
        val json = StringEntity("{ \"interests\": [\"donuts\"], \"apns\": { \"aps\": { \"alert\": \"Hi\" }}}")
        httpPost.setEntity(json)
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-Type", "application/json")
        httpPost.setHeader("Authorization", String.format("Bearer %s", this.secretKey))
        var response = client.execute(httpPost)
        System.out.println(response)
    }
}
