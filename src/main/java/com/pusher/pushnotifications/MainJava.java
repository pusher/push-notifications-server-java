package com.pusher.pushnotifications;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class MainJava {
    public MainJava() throws InterruptedException, IOException, URISyntaxException {
        String instanceId = "8f9a6e22-2483-49aa-8552-125f1a4c5781";
        String secretKey = "C54D42FB7CD2D408DDB22D7A0166F1D";

        List<String> interests = Arrays.asList("donuts", "pizza");

        Map<String, Map> publishRequest = new HashMap();
        Map<String, String> alert = new HashMap();
        alert.put("alert", "hi");
        Map<String, Map> aps = new HashMap();
        aps.put("aps", alert);
        publishRequest.put("apns", aps);

        PushNotifications pushNotifications = new PushNotifications(instanceId, secretKey);
        pushNotifications.publish(interests, publishRequest);
    }
}
