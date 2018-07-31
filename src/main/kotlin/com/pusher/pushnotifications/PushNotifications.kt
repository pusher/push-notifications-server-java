package com.pusher.pushnotifications

import com.google.gson.Gson
import java.io.IOException
import java.net.URISyntaxException
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

class PusherAuthError(val errorMessage: String): RuntimeException()
class PusherMissingInstanceError(val errorMessage: String): RuntimeException()
class PusherValidationError(val errorMessage: String): RuntimeException()
class PusherServerError(val errorMessage: String): RuntimeException()
data class PublishNotificationResponse(val publishId: String)
data class PushNotificationErrorResponse(val error: String, val description: String)

/**
 * Push Notifications class implements publish method
 * that is used to publish push notifications to specified interests.
 * @author www.pusher.com
 * @version 1.0.0
 *
 * @param instanceId the id of the instance
 * @param secretKey the secret key for the instance
 */
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

    /**
     * Publish the given publish_body to the specified interests.
     *
     * @param  interests List of interests that the publish body should be sent to.
     * @param  publishRequest Map containing the body of the push notification publish request.
     * @return publishId
     */
    @Throws(IOException::class, InterruptedException::class, URISyntaxException::class)
    fun publish(interests: List<String>, publishRequest: Map<String, Any>): String {
        this.validateInterests(interests)

        val publishRequestWithInterests = publishRequest.toMutableMap()
        publishRequestWithInterests.put("interests", interests)

        val client = HttpClients.createDefault()
        val httpPost = HttpPost("$baseURL/instances/$instanceId/publishes")
        httpPost.setEntity(StringEntity(gson.toJson(publishRequestWithInterests)))
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-Type", "application/json")
        httpPost.setHeader("Authorization", String.format("Bearer %s", this.secretKey))
        val response = client.execute(httpPost)
        val responseBody = EntityUtils.toString(response.entity, "UTF-8")
        val statusCode = response.statusLine.statusCode

        when (statusCode) {
            401 -> throw PusherAuthError(extractErrorDescription(responseBody))
            404 -> throw PusherMissingInstanceError(extractErrorDescription(responseBody))
            in 400..499 -> throw PusherValidationError(extractErrorDescription(responseBody))
            in 500..599 -> throw PusherServerError(extractErrorDescription(responseBody))
            else -> {
               return gson.fromJson(responseBody, PublishNotificationResponse::class.java).publishId
            }
        }
    }

    private fun extractErrorDescription(responseBody: String): String =
            gson.fromJson(responseBody, PushNotificationErrorResponse::class.java).description

    private fun validateInterests(interests: List<String>) {
        if (interests.isEmpty()) {
            throw IllegalArgumentException("Publish method expects at least one interest")
        }

        if (interests.count() == 1 && interests.first() == "") {
            throw IllegalArgumentException("interest should not be an empty string")
        }

        interests.find { it.length > interestsMaxLength }?.let {
            throw IllegalArgumentException("interest $it is longer than the maximum of $interestsMaxLength characters")
        }
    }
}
