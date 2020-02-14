package org.kasource.spring.nats.integration.inbound;


import org.springframework.context.SmartLifecycle;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.messaging.support.MessageBuilder;

import org.kasource.spring.nats.consumer.NatsConsumerManager;
import org.kasource.spring.nats.integration.NatsHeaders;

import io.nats.client.Message;


public class NatsInboundChannelAdapter extends MessageProducerSupport {

    private NatsConsumerManager natsConsumerManager;
    private Class<?> requestPayloadType;
    private String subject;
    private String queueName = "";
    private SmartLifecycle dispatcher;


    @Override
    public void onInit() {
        super.onInit();
        if (natsConsumerManager == null) {
            natsConsumerManager = this.getBeanFactory().getBean(NatsConsumerManager.class);
        }
        dispatcher = natsConsumerManager.register(this::onMessage, requestPayloadType, subject, queueName);
    }


    @Override
    protected void doStart() {
        dispatcher.start();
    }


    @Override
    protected void doStop() {
        dispatcher.stop();
    }


    protected org.springframework.messaging.Message<Object> buildMessage(Object object, Message msg) {
        return MessageBuilder.withPayload(object)
                .setHeader(NatsHeaders.SUBJECT, msg.getSubject())
                .setHeader(NatsHeaders.QUEUE, queueName)
                .setHeader(NatsHeaders.SUBSCRIPTION, msg.getSubscription())
                .setHeader(NatsHeaders.SUBSCRIPTION_ID, msg.getSID())
                .setHeader(NatsHeaders.REPLY_TO, msg.getReplyTo())
                .build();
    }

    private void onMessage(Object object, Message msg) {
        org.springframework.messaging.Message<Object> message = buildMessage(object, msg);
        try {
            this.sendMessage(message);
        } catch (RuntimeException e) {
            this.sendErrorMessageIfNecessary(message, e);
        }

    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }


    public void setRequestPayloadType(Class<?> requestPayloadType) {
        this.requestPayloadType = requestPayloadType;
    }

    public void setNatsConsumerManager(NatsConsumerManager natsConsumerManager) {
        this.natsConsumerManager = natsConsumerManager;
    }
}
