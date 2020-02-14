package org.kasource.spring.nats.integration.inbound;

import java.util.concurrent.TimeUnit;

import org.springframework.context.SmartLifecycle;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.support.utils.IntegrationUtils;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import org.kasource.spring.nats.NatsTemplate;
import org.kasource.spring.nats.consumer.NatsConsumerManager;
import org.kasource.spring.nats.integration.NatsHeaders;
import org.kasource.spring.nats.message.ErrorMessage;

import io.nats.client.Message;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NatsInboundGateway extends MessagingGatewaySupport {
    private static final Logger LOG = LoggerFactory.getLogger(NatsInboundGateway.class);

    private static final long DEFAULT_REQUEST_TIMEOUT = -1L;
    private static final int DEFAULT_EXECUTOR_MAX_THREADS = 30;

    private ThreadPoolTaskExecutor taskExecutor;
    private NatsTemplate natsTemplate;
    private Class<?> requestPayloadType;
    private String subject;
    private String queueName = "";
    private String errorSubject;
    private SmartLifecycle dispatcher;
    private NatsConsumerManager natsConsumerManager;
    private long requestTimeout = DEFAULT_REQUEST_TIMEOUT;


    public NatsInboundGateway(boolean errorOnTimeout) {
        super(errorOnTimeout);
    }

    @Override
    public void onInit() {
        super.onInit();
        if (natsTemplate == null) {
            natsTemplate = this.getBeanFactory().getBean(NatsTemplate.class);
        }
        if (natsConsumerManager == null) {
            natsConsumerManager = this.getBeanFactory().getBean(NatsConsumerManager.class);
        }
        if (taskExecutor == null) {
            taskExecutor = createExecutor();
        }

        dispatcher = natsConsumerManager.register(this::onMessage, requestPayloadType, subject, queueName);

    }

    private ThreadPoolTaskExecutor createExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setMaxPoolSize(DEFAULT_EXECUTOR_MAX_THREADS);
        threadPoolTaskExecutor.setThreadNamePrefix("NatsInboundGateway-");
        threadPoolTaskExecutor.afterPropertiesSet();
        return threadPoolTaskExecutor;
    }

    org.springframework.messaging.Message<?> sendAndReceiveMessage(org.springframework.messaging.Message<?> requestMessage) {
        return super.sendAndReceiveMessage(requestMessage);
    }

    @Override
    protected void doStart() {
        dispatcher.start();
    }


    @Override
    protected void doStop() {
        dispatcher.stop();
    }


    protected void onMessage(Object payload, Message msg) {
        final org.springframework.messaging.Message<Object> requestMessage = MessageBuilder.withPayload(payload)
                .setHeader(NatsHeaders.SUBJECT, subject)
                .setHeader(NatsHeaders.QUEUE, queueName)
                .setHeader(NatsHeaders.SUBSCRIPTION, msg.getSubscription())
                .setHeader(NatsHeaders.SUBSCRIPTION_ID, msg.getSID())
                .setHeader(NatsHeaders.REPLY_TO, msg.getReplyTo())
                .build();

        ListenableFuture<org.springframework.messaging.Message<?>> listenableFuture =
                taskExecutor.submitListenable(new InboundMessageTask(this, requestMessage));
        if (requestTimeout >= 0) {
            listenableFuture
                    .completable()
                    .orTimeout(requestTimeout, TimeUnit.MILLISECONDS)
                    .whenCompleteAsync((m, t) -> onResponse(msg, requestMessage, m, t));
        } else {
            listenableFuture
                    .completable()
                    .whenCompleteAsync((m, t) -> onResponse(msg, requestMessage, m, t));
        }

    }

    private String resolveReplyTopic(org.springframework.messaging.Message<?> responseMessage) {
        String replyToHeader = responseMessage.getHeaders().get(NatsHeaders.REPLY_TO, String.class);
        if (!StringUtils.isEmpty(replyToHeader)) {
            return replyToHeader;
        } else {
            throw new DestinationResolutionException("No " + NatsHeaders.REPLY_TO + " header value found, can't send message back.");
        }
    }

    protected void onResponse(Message natsMessage,
                            org.springframework.messaging.Message<?> requestMessage,
                            org.springframework.messaging.Message<?> responseMessage,
                            Throwable throwable) {
        Throwable error = throwable;
        if (responseMessage != null) {
            try {
                natsTemplate.publish(responseMessage.getPayload(), resolveReplyTopic(responseMessage));
            } catch (RuntimeException e) {
                error = e;
            }
        }
        if (error != null) {
            handleResponseError(natsMessage, requestMessage, error);
        } else {
            LOG.debug("Error already sent to the error-channel and handled there");

        }

    }

    private void handleResponseError(Message natsMessage,
                                     org.springframework.messaging.Message<?> requestMessage,
                                     Throwable throwable) {
        MessageChannel errorChannel = getErrorChannel();
        if (errorChannel != null && throwable instanceof Exception) {
            errorChannel.send(
                    MessageBuilder.withPayload(
                            IntegrationUtils.wrapInHandlingExceptionIfNecessary(requestMessage,
                            () -> "Error handling request on subject" + natsMessage.getSubject(),
                            (Exception) throwable))
                    .build());
        } else {
            if (!StringUtils.isEmpty(errorSubject)) {
                natsTemplate.publish(new ErrorMessage(throwable, natsMessage.getSubject()), errorSubject + "." + natsMessage.getReplyTo());
            }
            LOG.warn("Error handling request on subject" + natsMessage.getSubject(), throwable);
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

    public void setNatsTemplate(NatsTemplate natsTemplate) {
        this.natsTemplate = natsTemplate;
    }

    public void setNatsConsumerManager(NatsConsumerManager natsConsumerManager) {
        this.natsConsumerManager = natsConsumerManager;
    }



    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }


    public void setErrorSubject(String errorSubject) {
        this.errorSubject = errorSubject;
    }

    @Override
    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
}
