package org.kasource.spring.nats.integration.integration;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

public class SpringMessageListener<T> implements MessageHandler {

    private Optional<T> payload = Optional.empty();
    private CountDownLatch latch;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        payload = Optional.ofNullable((T) message.getPayload());
        if (latch != null) {
            latch.countDown();
        }
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }


    public Optional<T> getPayload() {
        return payload;
    }
}
