package org.kasource.spring.nats.integration.integration;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class NatsListener<T> implements Consumer<T> {

    private Optional<T> object = Optional.empty();

    private CountDownLatch latch;


    public Optional<T> getObject() {
        return object;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void accept(T object) {
        this.object = Optional.ofNullable(object);
        if (latch != null) {
            latch.countDown();
        }
    }
}
