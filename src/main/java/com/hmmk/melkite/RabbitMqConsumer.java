package com.hmmk.melkite;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class RabbitMqConsumer {

    @Incoming("my-channel") // Make sure this matches the channel in your application.properties
    @Blocking
    public void receive(String message) {
        System.out.println("Received message: " + message);
        // Process the message
    }
}
