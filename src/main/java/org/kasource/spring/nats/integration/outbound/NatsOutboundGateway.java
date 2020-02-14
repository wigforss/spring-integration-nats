package org.kasource.spring.nats.integration.outbound;

import java.util.concurrent.TimeUnit;

import org.springframework.expression.Expression;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.concurrent.CompletableToListenableFutureAdapter;
import org.springframework.util.concurrent.ListenableFuture;

import org.kasource.spring.nats.NatsResponse;
import org.kasource.spring.nats.NatsTemplate;
import org.kasource.spring.nats.integration.NatsHeaders;

import org.apache.commons.lang.StringUtils;

public class NatsOutboundGateway extends AbstractReplyProducingMessageHandler {
    private static final long DEFAULT_REQUEST_TIMEOUT_MILLIS = -1L;

    private String subject;
    private Expression subjectExpression;
    private Class<?> replyPayloadType;
    private NatsTemplate natsTemplate;
    private long requestTimeout = DEFAULT_REQUEST_TIMEOUT_MILLIS;


    @Override
    protected void doInit()  {
        Assert.isTrue(!StringUtils.isEmpty(subject) || subjectExpression != null, "either subject or subject-expression must be set");
        if (natsTemplate == null) {
            natsTemplate = this.getBeanFactory().getBean(NatsTemplate.class);
        }
    }

    @Override
    protected Object handleRequestMessage(Message<?> message) {

        if (this.isAsync()) {
            return handleAsyncRequestResponse(message, resolveSubject(message));
        } else {
            return handleRequestResponse(message, resolveSubject(message));
        }
    }

    private String resolveSubject(Message<?> message) {
        String recipientSubject = subject;
        if (recipientSubject == null) {
            recipientSubject = subjectExpression.getValue(message, String.class);
        }
        if (!StringUtils.isEmpty(recipientSubject)) {
            return recipientSubject;
        } else {
            throw new DestinationResolutionException("Can't resolve subject for outbound message: " + message.getHeaders().getId());
        }
    }

    private Message<?> handleRequestResponse(Message<?> message, String recipientSubject) {
        NatsResponse natsResponse = natsTemplate.request(
                message.getPayload(),
                recipientSubject,
                replyPayloadType,
                requestTimeout,
                TimeUnit.MILLISECONDS);
        return toMessage(natsResponse);
    }



    private ListenableFuture<? extends Message<?>> handleAsyncRequestResponse(Message<?> message, String recipientSubject) {
        if (requestTimeout >= 0) {
            return new CompletableToListenableFutureAdapter<>(natsTemplate.requestAsync(message.getPayload(),
                                                                                        recipientSubject,
                                                                                        replyPayloadType)
                    .orTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                    .thenApplyAsync(r -> toMessage(r)));
        } else {
            return new CompletableToListenableFutureAdapter<>(natsTemplate.requestAsync(message.getPayload(),
                                                                                        recipientSubject,
                                                                                        replyPayloadType)
                    .thenApplyAsync(r -> toMessage(r)));
        }

    }



    private Message<?> toMessage(NatsResponse response) {
        return  MessageBuilder.withPayload(response.getPayload())
                .setHeader(NatsHeaders.SUBJECT, response.getMessage().getSubject())
                .setHeader(NatsHeaders.SUBSCRIPTION, response.getMessage().getSubscription())
                .setHeader(NatsHeaders.SUBSCRIPTION_ID, response.getMessage().getSID())
                .setHeader(NatsHeaders.REPLY_TO, response.getMessage().getReplyTo())
                .build();
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSubjectExpression(Expression subjectExpression) {
        this.subjectExpression = subjectExpression;
    }

    public void setReplyPayloadType(Class<?> replyPayloadType) {
        this.replyPayloadType = replyPayloadType;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public void setNatsTemplate(NatsTemplate natsTemplate) {
        this.natsTemplate = natsTemplate;
    }
}
