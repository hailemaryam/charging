package com.hmmk.melkite;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class RabbitMqProducer {

    @Inject
    @Channel("my-outgoing-channel")
    Emitter<String> emitter;  // Make sure this matches the outgoing channel

    public void sendMessage(String message) {
        emitter.send(message).whenComplete((success, failure) -> {
            if (failure != null) {
                System.out.println("Failed to send message: " + failure.getMessage());
            } else {
                System.out.println("Message sent successfully: " + message);
            }
        });
    }
}
