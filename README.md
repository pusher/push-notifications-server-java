# Beams Java Server SDK

[![Build Status](https://travis-ci.org/pusher/push-notifications-server-java.svg?branch=master)](https://travis-ci.org/pusher/push-notifications-server-java)

## Installation

The compiled library is available in two ways:

### Maven

The push-notifications-server-java is available in Maven Central.

```xml
<dependencies>
    <dependency>
      <groupId>com.pusher</groupId>
      <artifactId>push-notifications-server-java</artifactId>
      <version>0.9.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
dependencies {
  compile 'com.pusher:push-notifications-server-java:0.9.0'
}
```

### Download

You can download a version of the `.jar` directly from [Maven](http://repo1.maven.org/maven2/com/pusher/push-notifications-server-java/).

## Usage
### Configuring the SDK for Your Instance

Use your instance id and secret (you can get these from the [dashboard](https://dash.pusher.com/beams)) to create a Beams PushNotifications instance:
<details><summary>Java</summary>
<p>

```java
String instanceId = "8f9a6e22-2483-49aa-8552-125f1a4c5781";
String secretKey = "C54D42FB7CD2D408DDB22D7A0166F1D";

PushNotifications pushNotifications = new PushNotifications(instanceId, secretKey);
```

</p>
</details>

<details><summary>Kotlin</summary>
<p>

```kotlin
val instanceId = "8f9a6e22-2483-49aa-8552-125f1a4c5781"
val secretKey = "C54D42FB7CD2D408DDB22D7A0166F1D"

val pn = PushNotifications(instanceId, secretKey)
```

</p>
</details>

### Publishing a Notification
Once you have created your PushNotifications instance you can publish a push notification to your registered & subscribed devices:
<details><summary>Java</summary>
<p>

```java
List<String> interests = Arrays.asList("donuts", "pizza");

Map<String, Map> publishRequest = new HashMap();

Map<String, String> alert = new HashMap();
alert.put("alert", "hi");
Map<String, Map> aps = new HashMap();
aps.put("aps", alert);
publishRequest.put("apns", aps);

Map<String, String> fcmNotification = new HashMap();
fcmNotification.put("title", "hello");
fcmNotification.put("body", "Hello world");
Map<String, Map> fcm = new HashMap();
fcm.put("notification", fcmNotification);
publishRequest.put("fcm", fcm);

pushNotifications.publish(interests, publishRequest);
```

</p>
</details>

<details><summary>Kotlin</summary>
<p>

```kotlin
val interests = listOf("donuts", "pizza")
val publishRequest = hashMapOf(
  "apns" to hashMapOf("aps" to hashMapOf("alert" to "hi")),
  "fcm" to hashMapOf("notification" to hashMapOf("title" to "hello", "body" to "Hello world"))
)

pn.publish(interests, publishRequest)
```

</p>
</details>
