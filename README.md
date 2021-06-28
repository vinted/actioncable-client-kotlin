# actioncable-client-kotlin

[![Release](https://jitpack.io/v/vinted/actioncable-client-kotlin.svg)](https://jitpack.io/#vinted/actioncable-client-kotlin)
[![Build Status](https://travis-ci.com/vinted/actioncable-client-kotlin.svg?branch=master)](https://travis-ci.com/vinted/actioncable-client-kotlin)

Ruby [Action Cable](http://guides.rubyonrails.org/action_cable_overview.html) client library for Kotlin.

## About Vinted's fork

This is a fork of original library: https://github.com/hosopy/actioncable-client-kotlin

Main changes compared to original version:

* Introduced stable version of Kotlin coroutines
* Updated okhttp 2.x ---> okhttp 3.x
* Changed serializer klaxon ---> gson

## Contribution

Pull requests are always welcome.

# Usage

## Requirements

* Kotlin 1.3 or later
* [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) 1.3.x or later
* [Gson](https://github.com/google/gson) 2.x 
* [okhttp](https://github.com/square/okhttp) 3.x

## Download

```groovy
repositories {
    jcenter()
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.vinted:actioncable-client-kotlin:<version>'
}
```

## Basic

```kotlin
// Setup
val uri = URI("ws://cable.example.com")
var consumer = ActionCable.createConsumer(uri)

// Create subscription
val fooChannel = Channel("FooChannel")
val subscription = consumer.subscriptions.create(fooChannel)

subscription.onConnected = {
    // Called when the subscription has been successfully completed
}

subscription.onRejected = {
    // Called when the subscription is rejected by the server
}

subscription.onReceived = { data: Any? ->
    // Called when the subscription receives data from the server
    // Possible types...
    when (data) {
        is Int -> { }
        is Long -> { }
        is BigInteger -> { }
        is String -> { }
        is Double -> { }
        is Boolean -> { }
        is JsonObject -> { }
        is JsonArray<*> -> { }
    }
}

subscription.onDisconnected = {
    // Called when the subscription has been closed
}

subscription.onFailed = { error ->
    // Called when the subscription encounters any error
}

// Establish connection
consumer.connect()

// Perform any action
subscription.perform("away")

// Perform any action with params
subscription.perform("hello", mapOf("name" to "world"))

// Terminate connection
consumer.disconnect()
consumer = null
```

Few notes:
- You can create new channels and instantiate corresponding subscriptions after ```consumer.connect()``` call. 
- It is allowed to have multiple subscriptions for the single channel. 
- You can check if at least one subscription exists for the particular channel via: ```consumer.subscriptions.contains(channel)```

## Passing Parameters to Channel

```kotlin
val chatChannel = Channel("ChatChannel", mapOf("room_id" to 1))
```

The parameter container is `Map<String, Any?>` and is converted to `JsonObject(Gson)` internally.
To know what type of value can be passed, please read [Gson user guide](https://github.com/google/gson).

## Sending Data via Subscription

```kotlin
subscription.perform("send", mapOf(
    "comment" to mapOf(
        "text" to "This is string.",
        "private" to true,
        "images" to arrayOf(
            "http://example.com/image1.jpg",
            "http://example.com/image2.jpg"
        )
    )
))
```

The parameter container is `Map<String, Any?>` and is converted to `JsonObject(Gson)` internally.
To know what type of value can be passed, please read [Gson user guide](https://github.com/google/gson).

## Options

```kotlin
val uri = URI("ws://cable.example.com")
val options = Consumer.Options()
options.connection.reconnection = true

val consumer = ActionCable.createConsumer(uri, options)
```

Below is a list of available options.

* sslContext

    ```kotlin
    options.connection.sslContext = yourSSLContextInstance
    ```

* hostnameVerifier

    ```kotlin
    options.connection.hostnameVerifier = yourHostnameVerifier
    ```

* query
    
    ```kotlin
    options.connection.query = mapOf("user_id" to "1")
    ```
    
* headers
    
    ```kotlin
    options.connection.headers = mapOf("X-Foo" to "Bar")
    ```
    
* reconnection
    * If reconnection is true, the client attempts to reconnect to the server when underlying connection is stale.
    * Default is `false`.
    
    ```kotlin
    options.connection.reconnection = false
    ```
    
* reconnectionMaxAttempts
    * The maximum number of attempts to reconnect.
    * Default is `30`.
    
    ```kotlin
    options.connection.reconnectionMaxAttempts = 30
    ```

* webSocketFactory
    * Pass your own instance of OkHttp `WebSocket.Factory`
    * Note: if you provide a custom `WebSocket.Factory` implementation, then `sslContext` and `hostnameVerifier` options are ignored. You must set those options directly in your custom implementation instead of setting it on `options.connection`. 

    ```kotlin
    options.connection.webSocketFactory = OkHttpClient.Builder().apply {
        // Configure your OkHttpClient how you like
        networkInterceptors().add(StethoInterceptor())
        sslSocketFactory(yourSocketFactory)
        hostnameVerifier(yourHostnameVerifier)
    }.build()
    ```

## Authentication

How to authenticate a request depends on the architecture you choose.

### Authenticate by HTTP Header

```kotlin
val options = Consumer.Options()
options.connection.headers = mapOf("Authorization" to "Bearer xxxxxxxxxxx")

val consumer = ActionCable.createConsumer(uri, options)
```

### Authenticate by Query Params

```kotlin
val options = Consumer.Options()
options.connection.query = mapOf("access_token" to "xxxxxxxxxxx")

val consumer = ActionCable.createConsumer(uri, options)
```

### License

```
MIT License

Copyright (c) 2021 Vinted UAB

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
