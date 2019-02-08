package com.pusher.pushnotifications.example

import com.pusher.pushnotifications.PushNotifications
import java.io.IOException
import java.net.URISyntaxException

object MainKotlin {

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val instanceId = "8f9a6e22-2483-49aa-8552-125f1a4c5781"
            val secretKey = "C54D42FB7CD2D408DDB22D7A0166F1D"
            val interests = listOf("donuts", "pizza")
            val publishRequest = hashMapOf("apns" to hashMapOf("aps" to hashMapOf("alert" to "hi")))

            val pn = PushNotifications(instanceId, secretKey)
            pn.publishToInterests(interests, publishRequest)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
}
