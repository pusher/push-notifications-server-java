package com.pusher

import com.google.gson.Gson
import com.google.gson.JsonArray
import java.io.IOException
import java.net.URISyntaxException
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients


class PushNotifications(private val instanceId: String, private val secretKey: String) {

    private val gson = Gson()
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
    fun publish(interests: List<String>, publishRequest: Map<String, Any>) {
        this.validateInput(interests)

        val publishRequestWithInterests = publishRequest.toMutableMap()
        publishRequestWithInterests.put("interests", interests)

        val client = HttpClients.createDefault()
        val url = String.format("$baseURL/instances/%s/publishes", this.instanceId)
        val httpPost = HttpPost(url)
        httpPost.setEntity(StringEntity(gson.toJson(publishRequestWithInterests)))
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-Type", "application/json")
        httpPost.setHeader("Authorization", String.format("Bearer %s", this.secretKey))
        val response = client.execute(httpPost)
        System.out.println(response)
    }

    private fun validateInput(interests: List<String>) {
        if (interests.isEmpty()) {
            throw IllegalArgumentException("Publish method expects at least one interest")
        }

        interests.find { it.length > interestsMaxLength }?.let {
            throw IllegalArgumentException(String.format("interest %s is longer than the maximum of %d characters", it, interestsMaxLength))
        }
    }
}
