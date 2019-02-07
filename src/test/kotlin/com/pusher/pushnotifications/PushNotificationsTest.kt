package com.pusher.pushnotifications

import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.IllegalArgumentException

class PushNotificationsTest {
    private val validInstanceId = "9aa32e04-a212-44ab-a592-9aeba66e46ac"
    private val validSecretKey = "188C879D394E09FDECC04606A126FAE2125FEABD24A2D12C6AC969AE1CEE2AEC"
    private val beams = PushNotifications(validInstanceId, validSecretKey)

    private val validPublishRequest = hashMapOf("apns" to hashMapOf("aps" to hashMapOf("alert" to "hi")))

    // constructor

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception if instanceId is empty`() {
        PushNotifications("", validSecretKey)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `should throw an exception if secretKet is empty`() {
        PushNotifications(validInstanceId, "")
    }

    // generateToken

    @Test(expected = IllegalArgumentException::class)
    fun `generateToken should throw an exception if the user id is too long`() {
        val longUserId = String(ByteArray(200))
        beams.generateToken(longUserId)
    }

    @Test
    fun `generateToken should generate a token`() {
        val token = beams.generateToken("user123")

        assertThat(token["token"], `is`(notNullValue()))
        assertTrue((token["token"] as String).length > 200)
    }

    @Test
    fun `generateToken should generate a token based on a 248 bit key`() {
        val beamsWithSmallerKey = PushNotifications(validInstanceId, "F7D28BD68A9DB989ADF6EB19F89B3DB")
        val token = beamsWithSmallerKey.generateToken("user123")

        assertThat(token["token"], `is`(notNullValue()))
        assertTrue((token["token"] as String).length > 200)
    }

    // publishToInterests

    @Test(expected = IllegalArgumentException::class)
    fun `publishToInterests should fail if interests is empty`() {
        beams.publishToInterests(emptyList(), validPublishRequest)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `publishToInterests should fail if too many interests are given`() {
        val manyInterests = MutableList(9001) { index -> index.toString() }

        beams.publishToInterests(manyInterests, validPublishRequest)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `publishToInterests should fail an interest is too long`() {
        val someLongInterests = listOf("short-interest", "long-interest-${String(ByteArray(200))})")

        beams.publishToInterests(someLongInterests, validPublishRequest)
    }

    @Test
    fun `publishToInterests should return a publish id if everything is correct`() {
        val publishId = beams.publishToInterests(listOf("hello"), validPublishRequest)

        assertThat(publishId, `is`(notNullValue()))
    }

    // publishToUsers

    @Test(expected = IllegalArgumentException::class)
    fun `publishToUsers should fail if users is empty`() {
        beams.publishToUsers(emptyList(), validPublishRequest)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `publishToUsers should fail if too many users are given`() {
        val manyUsers = MutableList(9001) { index -> index.toString() }

        beams.publishToUsers(manyUsers, validPublishRequest)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `publishToUsers should fail an user is too long`() {
        val someLongUsers = listOf("short-user", "long-user-${String(ByteArray(200))})")

        beams.publishToUsers(someLongUsers, validPublishRequest)
    }

    @Test
    fun `publishToUsers should return a publish id if everything is correct`() {
        val publishId = beams.publishToUsers(listOf("hello"), validPublishRequest)

        assertThat(publishId, `is`(notNullValue()))
    }

    // deleteUser

    @Test(expected = IllegalArgumentException::class)
    fun `deleteUser should throw an exception if the user id is too long`() {
        val longUserId = String(ByteArray(200))
        beams.deleteUser(longUserId)
    }

    @Test
    fun `deleteUser should not return errors if the user id is valid`() {
        beams.deleteUser("java-user")
        // no exceptions --> great
    }
}
