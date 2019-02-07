package com.pusher.pushnotifications

import com.auth0.jwt.algorithms.Algorithm
import com.google.gson.Gson
import org.apache.http.client.methods.HttpDelete
import java.io.IOException
import java.net.URISyntaxException
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import com.auth0.jwt.JWT

class PusherAuthError(val errorMessage: String): RuntimeException()
class PusherTooManyRequestsError(val errorMessage: String): RuntimeException()
class PusherMissingInstanceError(val errorMessage: String): RuntimeException()
class PusherValidationError(val errorMessage: String): RuntimeException()
class PusherServerError(val errorMessage: String): RuntimeException()
data class PublishNotificationResponse(val publishId: String)
data class PushNotificationErrorResponse(val error: String, val description: String)

/**
 * Push Notifications class implements publish method
 * that is used to publish push notifications to specified interests.
 *
 * @author www.pusher.com
 * @version 1.0.0
 *
 * @param instanceId the id of the instance
 * @param secretKey the secret key for the instance
 */
class PushNotifications(private val instanceId: String, private val secretKey: String) {
    private val gson = Gson()
    private val interestNameMaxLength = 164
    private val maxRequestInterestsAllowed = 100
    private val userIdMaxLength = 164
    private val maxRequestUsersAllowed = 1000
    private val baseURL = "https://$instanceId.pushnotifications.pusher.com"

    init {
        if (instanceId.isEmpty()) {
            throw IllegalArgumentException("instanceId can't be an empty string")
        }

        if (secretKey.isEmpty()) {
            throw IllegalArgumentException("secretKey can't be an empty string")
        }
    }

    /**
     * Generates an auth token which will allow devices to associate themselves with the given user id
     *
     * @param userId User id for which the token will be valid
     * @return Beams token in a Map for JSON serialization
     */
    fun generateToken(userId: String): Map<String, Any> {
        if (userId.length > userIdMaxLength) {
            throw IllegalArgumentException(
                    "User id ($userId) is too long (expected less than ${userIdMaxLength+1}, got ${userId.length})")
        }

        val iss = "https://$instanceId.pushnotifications.pusher.com"
        val exp = LocalDateTime.now().plusDays(1)

        val token = JWT.create()
                .withSubject(userId)
                .withIssuer(iss)
                .withExpiresAt(Date.from(exp.toInstant(ZoneOffset.UTC)))
                .sign(Algorithm.HMAC256(secretKey))

        return mapOf("token" to token)
    }

    /**
     * Publish the given publish_body to the specified interests.
     *
     * @param  interests List of interests that the publish body should be sent to.
     * @param  publishRequest Map containing the body of the push notification publish request.
     * @return publishId
     * @deprecated use publishToInterests instead
     */
    @Deprecated("use publishToInterests instead", ReplaceWith("publishToInterests(interests, publishRequest)"))
    @Throws(IOException::class, InterruptedException::class, URISyntaxException::class)
    fun publish(interests: List<String>, publishRequest: Map<String, Any>): String {
        return publishToInterests(interests, publishRequest)
    }

    /**
     * Publish the given publish_body to the specified interests.
     *
     * @param  interests List of interests that the publish body should be sent to.
     * @param  publishRequest Map containing the body of the push notification publish request.
     * @return publishId
     */
    @Throws(IOException::class, InterruptedException::class, URISyntaxException::class)
    fun publishToInterests(interests: List<String>, publishRequest: Map<String, Any>): String {
        if (interests.isEmpty()) {
            throw IllegalArgumentException("Publish method expects at least one interest")
        }

        if (interests.count() > maxRequestInterestsAllowed) {
            throw IllegalArgumentException("publish requests can only have up to $maxRequestInterestsAllowed interests (given ${interests.count()})")
        }

        interests.find { it.length > interestNameMaxLength }?.let {
            throw IllegalArgumentException("interest $it is longer than the maximum of $interestNameMaxLength characters")
        }

        val publishRequestWithInterests = publishRequest.toMutableMap()
        publishRequestWithInterests.put("interests", interests)

        val client = HttpClients.createDefault()
        val httpPost = HttpPost("$baseURL/publish_api/v1/instances/$instanceId/publishes/interests")
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
            429 -> throw PusherTooManyRequestsError(extractErrorDescription(responseBody))
            in 400..499 -> throw PusherValidationError(extractErrorDescription(responseBody))
            in 500..599 -> throw PusherServerError(extractErrorDescription(responseBody))
            else -> {
               return gson.fromJson(responseBody, PublishNotificationResponse::class.java).publishId
            }
        }
    }

    /**
     * Publish the given publish_body to the specified users.
     *
     * @param  users List of user ids that the publish body should be sent to.
     * @param  publishRequest Map containing the body of the push notification publish request.
     * @return publishId
     */
    @Throws(IOException::class, InterruptedException::class, URISyntaxException::class)
    fun publishToUsers(users: List<String>, publishRequest: Map<String, Any>): String {
        if (users.isEmpty()) {
            throw IllegalArgumentException("Publish method expects at least one user")
        }

        if (users.count() > maxRequestUsersAllowed) {
            throw IllegalArgumentException("publish requests can only have up to $maxRequestUsersAllowed users (given ${users.count()})")
        }

        users.find { it.length > userIdMaxLength }?.let {
            throw IllegalArgumentException("user id $it is longer than the maximum of $userIdMaxLength characters")
        }

        val publishRequestWithUsers = publishRequest.toMutableMap()
        publishRequestWithUsers.put("users", users)

        val client = HttpClients.createDefault()
        val httpPost = HttpPost("$baseURL/publish_api/v1/instances/$instanceId/publishes/users")
        httpPost.setEntity(StringEntity(gson.toJson(publishRequestWithUsers)))
        httpPost.setHeader("Accept", "application/json")
        httpPost.setHeader("Content-Type", "application/json")
        httpPost.setHeader("Authorization", String.format("Bearer %s", this.secretKey))
        val response = client.execute(httpPost)
        val responseBody = EntityUtils.toString(response.entity, "UTF-8")
        val statusCode = response.statusLine.statusCode

        when (statusCode) {
            401 -> throw PusherAuthError(extractErrorDescription(responseBody))
            404 -> throw PusherMissingInstanceError(extractErrorDescription(responseBody))
            429 -> throw PusherTooManyRequestsError(extractErrorDescription(responseBody))
            in 400..499 -> throw PusherValidationError(extractErrorDescription(responseBody))
            in 500..599 -> throw PusherServerError(extractErrorDescription(responseBody))
            else -> {
                return gson.fromJson(responseBody, PublishNotificationResponse::class.java).publishId
            }
        }
    }

    /**
     * Remove the user with the given ID (and all of their devices) from the Pusher Beams database.
     * The user will no longer receive any notifications. This action cannot be undone.
     *
     * @param userId id of the user to be deleted
     */
    fun deleteUser(userId: String) {
        if (userId.length > userIdMaxLength) {
            throw IllegalArgumentException(
                    "User id ($userId) is too long (expected less than ${userIdMaxLength + 1}, got ${userId.length})")
        }

        val userIdURLEncoded = URLEncoder.encode(userId, "UTF-8")

        val httpDelete = HttpDelete("$baseURL/customer_api/v1/instances/$instanceId/users/$userIdURLEncoded")
        httpDelete.setHeader("Authorization", String.format("Bearer %s", this.secretKey))

        val client = HttpClients.createDefault()
        val response = client.execute(httpDelete)
        val responseBody = EntityUtils.toString(response.entity, "UTF-8")
        val statusCode = response.statusLine.statusCode

        when (statusCode) {
            401 -> throw PusherAuthError(extractErrorDescription(responseBody))
            404 -> throw PusherMissingInstanceError(extractErrorDescription(responseBody))
            429 -> throw PusherTooManyRequestsError(extractErrorDescription(responseBody))
            in 400..499 -> throw PusherValidationError(extractErrorDescription(responseBody))
            in 500..599 -> throw PusherServerError(extractErrorDescription(responseBody))
            else -> {
                return // great
            }
        }
    }

    private fun extractErrorDescription(responseBody: String): String =
            gson.fromJson(responseBody, PushNotificationErrorResponse::class.java).description
}
