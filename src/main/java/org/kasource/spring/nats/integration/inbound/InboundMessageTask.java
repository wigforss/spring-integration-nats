package org.kasource.spring.nats.integration.inbound;

import java.util.concurrent.Callable;

import org.springframework.messaging.Message;


public class InboundMessageTask implements Callable<Message<?>> {

    private NatsInboundGateway inboundGateway;
    private Message<?> message;

    public InboundMessageTask(final NatsInboundGateway inboundGateway, final Message<?> message) {
        this.inboundGateway = inboundGateway;
        this.message = message;

    }

    @Override
    public Message<?> call() throws Exception {

        return inboundGateway.sendAndReceiveMessage(message);

    }


}
