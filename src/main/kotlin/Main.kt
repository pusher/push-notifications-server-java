package com.pusher

import java.io.IOException
import java.net.URISyntaxException

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            val instanceId = "8f9a6e22-2483-49aa-8552-125f1a4c5781"
            val secretKey = "C54D42FB7CD2D408DDB22D7A0166F1D"
            val payload = "{ \"interests\": [\"donuts\"], \"apns\": { \"aps\": { \"alert\": \"Hi\" }}}"

            val pn = PushNotifications(instanceId, secretKey)
            pn.publish(payload)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
}
