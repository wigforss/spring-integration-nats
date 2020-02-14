package org.kasource.spring.nats.integration.inbound;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import static org.kasource.spring.nats.integration.NatsHeaders.REPLY_TO;
import org.kasource.spring.nats.NatsTemplate;
import org.kasource.spring.nats.consumer.NatsConsumerManager;
import org.kasource.spring.nats.message.ErrorMessage;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import io.nats.client.Message;
import io.nats.client.Subscription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.inject.util.InjectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class NatsInboundGatewayTest {
    @Mock
    private ThreadPoolTaskExecutor taskExecutor;
    @Mock
    private NatsConsumerManager natsConsumerManager;
    @Mock
    private NatsTemplate natsTemplate;
    @Mock
    private BeanFactory beanFactory;
    @Mock
    private SmartLifecycle dispatcher;
    @Mock
    private Object payload;
    @Mock
    private Message message;
    @Mock
    private Subscription subscription;
    @Mock
    private ListenableFuture<org.springframework.messaging.Message<?>> listenableFuture;
    @Mock
    private CompletableFuture<org.springframework.messaging.Message<?>> completableFuture;
    @Mock
    private org.springframework.messaging.Message<?> requestMessage;
    @Mock
    private org.springframework.messaging.Message<?> responseMessage;
    @Mock
    private RuntimeException runtimeException;
    @Mock
    private MessageHeaders messageHeaders;
    @Mock
    private MessageChannel errorChannel;

    @Captor
    private ArgumentCaptor<Object> payloadCaptor;

    @Captor
    private ArgumentCaptor<ErrorMessage> errorMessageCaptor;

    @Captor
    private ArgumentCaptor<org.springframework.messaging.Message<?>> springErrorMessageCaptor;

    private Class<?> requestPayloadType = String.class;
    private String subject = "subject";
    private String queueName = "queueName";

    private long requestTimeout = -1L;

    private NatsInboundGateway inboundGateway = new NatsInboundGateway(true);

    @Before
    public void setup() {
        inboundGateway.setTaskExecutor(taskExecutor);
        inboundGateway.setNatsConsumerManager(natsConsumerManager);
        inboundGateway.setNatsTemplate(natsTemplate);
        inboundGateway.setSubject(subject);
        inboundGateway.setQueueName(queueName);
        inboundGateway.setRequestPayloadType(requestPayloadType);
        inboundGateway.setRequestTimeout(requestTimeout);
        InjectionUtils.injectInto(dispatcher, inboundGateway, "dispatcher");
    }

    @Test
    public void initLookupDeps() {
        InjectionUtils.injectInto(null, inboundGateway, "natsTemplate");
        InjectionUtils.injectInto(null, inboundGateway, "natsConsumerManager");
        InjectionUtils.injectInto(null, inboundGateway, "taskExecutor");
        InjectionUtils.injectInto(beanFactory, inboundGateway, "beanFactory");

        when(beanFactory.getBean(NatsTemplate.class)).thenReturn(natsTemplate);
        when(beanFactory.getBean(NatsConsumerManager.class)).thenReturn(natsConsumerManager);

        inboundGateway.onInit();

        verify(natsConsumerManager).register(
                Mockito.isA(BiConsumer.class),
                eq(requestPayloadType),
                eq(subject),
                eq(queueName));

    }

    @Test
    public void initWithDeps() {
        InjectionUtils.injectInto(beanFactory, inboundGateway, "beanFactory");

        inboundGateway.onInit();

        verify(beanFactory, times(0)).getBean(NatsTemplate.class);
        verify(beanFactory, times(0)).getBean(NatsConsumerManager.class);

        verify(natsConsumerManager).register(
                Mockito.isA(BiConsumer.class),
                eq(requestPayloadType),
                eq(subject),
                eq(queueName));

    }

    @Test
    public void start() {
        inboundGateway.doStart();

        verify(dispatcher).start();
    }

    @Test
    public void stop() {
        inboundGateway.doStop();

        verify(dispatcher).stop();
    }

    @Test
    public void onMessage() {
        String subscriptionId = "subscriptionId";
        String replyToSubject = "replyTo";

        when(message.getSubscription()).thenReturn(subscription);
        when(message.getSID()).thenReturn(subscriptionId);
        when(message.getReplyTo()).thenReturn(replyToSubject);
        when(taskExecutor.submitListenable(Mockito.isA(InboundMessageTask.class))).thenReturn(listenableFuture);
        when(listenableFuture.completable()).thenReturn(completableFuture);


        inboundGateway.onMessage(payload, message);

        verify(completableFuture).whenCompleteAsync(Mockito.isA(BiConsumer.class));
    }

    @Test
    public void onMessageWithTimeout() {
        final long anotherTimeout = 1000L;
        InjectionUtils.injectInto(anotherTimeout, inboundGateway, "requestTimeout");
        String subscriptionId = "subscriptionId";
        String replyToSubject = "replyTo";

        when(message.getSubscription()).thenReturn(subscription);
        when(message.getSID()).thenReturn(subscriptionId);
        when(message.getReplyTo()).thenReturn(replyToSubject);
        when(taskExecutor.submitListenable(Mockito.isA(InboundMessageTask.class))).thenReturn(listenableFuture);
        when(listenableFuture.completable()).thenReturn(completableFuture);
        when(completableFuture.orTimeout(anotherTimeout, TimeUnit.MILLISECONDS)).thenReturn(completableFuture);

        inboundGateway.onMessage(payload, message);

        verify(completableFuture).whenCompleteAsync(Mockito.isA(BiConsumer.class));
    }

    @Test
    public void onSuccessfulResponse() {
        String replyToSubject = "replyToSubject";

        when(responseMessage.getPayload()).thenAnswer(a -> payload);
        when(responseMessage.getHeaders()).thenReturn(messageHeaders);
        when(messageHeaders.get(REPLY_TO, String.class)).thenReturn(replyToSubject);

        inboundGateway.onResponse(message, requestMessage, responseMessage, null);

        verify(natsTemplate).publish(payloadCaptor.capture(), eq(replyToSubject));

        assertThat(payloadCaptor.getValue(), is(equalTo(payload)));

    }

    @Test
    public void onResponseNoReplyToHeader() {
        when(responseMessage.getPayload()).thenAnswer(a -> payload);
        when(responseMessage.getHeaders()).thenReturn(messageHeaders);
        when(messageHeaders.get(REPLY_TO, String.class)).thenReturn(null);

        inboundGateway.onResponse(message, requestMessage, responseMessage, null);

    }

    @Test
    public void onErrorResponse() {
        String errorSubject = "errorSubject";
        InjectionUtils.injectInto(errorSubject, inboundGateway, "errorSubject");

        String replyToSubject = "replyToSubject";


        when(message.getSubject()).thenReturn(subject);
        when(message.getReplyTo()).thenReturn(replyToSubject);
        inboundGateway.onResponse(message, requestMessage, null, runtimeException);

        verify(natsTemplate).publish(errorMessageCaptor.capture(), eq(errorSubject + "." + replyToSubject));

        assertThat(errorMessageCaptor.getValue().getSubject(), is(equalTo(subject)));
        assertThat(errorMessageCaptor.getValue().getExceptionType(), is(equalTo(runtimeException.getClass().getName())));

    }

    @Test
    public void onErrorResponseWithErrorChannel() {

        String errorSubject = "errorSubject";
        String replyToSubject = "replyToSubject";
        InjectionUtils.injectInto(errorChannel, inboundGateway, "errorChannel");
        InjectionUtils.injectInto(errorSubject, inboundGateway, "errorSubject");

        when(message.getSubject()).thenReturn(subject);

        inboundGateway.onResponse(message, requestMessage, null, runtimeException);

        verify(errorChannel).send(springErrorMessageCaptor.capture());

        MessagingException messagingException = (MessagingException) springErrorMessageCaptor.getValue().getPayload();

        assertThat(messagingException.getCause() , is(equalTo(runtimeException)));
        assertThat(messagingException.getFailedMessage(), is(equalTo(requestMessage)));

    }
}
