package org.kasource.spring.nats.integration.outbound;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.Expression;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.core.DestinationResolutionException;

import static org.kasource.spring.nats.integration.NatsHeaders.REPLY_TO;
import static org.kasource.spring.nats.integration.NatsHeaders.SUBJECT;
import static org.kasource.spring.nats.integration.NatsHeaders.SUBSCRIPTION;
import static org.kasource.spring.nats.integration.NatsHeaders.SUBSCRIPTION_ID;
import org.kasource.spring.nats.NatsResponse;
import org.kasource.spring.nats.NatsTemplate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import io.nats.client.Subscription;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.inject.util.InjectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class NatsOutboundGatewayTest {
    private String subject = "subject";
    @Mock
    private Expression subjectExpression;
    private Class<?> replyPayloadType = String.class;
    @Mock
    private NatsTemplate natsTemplate;
    @Mock
    private BeanFactory beanFactory;
    @Mock
    private Message<?> requestMessage;
    @Mock
    private NatsResponse natsResponse;
    @Mock
    private Object requestPayload;
    @Mock
    private Object responsePayload;
    @Mock
    private io.nats.client.Message natsResponseMessage;
    @Mock
    private Subscription subscription;
    @Mock
    private CompletableFuture<NatsResponse> completableFuture;
    @Mock
    private MessageHeaders messageHeaders;
    private long requestTimeout = -1L;

    private NatsOutboundGateway outboundGateway = new NatsOutboundGateway();

    @Before
    public void setup() {
        outboundGateway.setNatsTemplate(natsTemplate);
        outboundGateway.setReplyPayloadType(replyPayloadType);
        outboundGateway.setRequestTimeout(requestTimeout);
        outboundGateway.setSubject(subject);
        InjectionUtils.injectInto(beanFactory, outboundGateway, "beanFactory");
    }

    @Test
    public void init() {
        outboundGateway.doInit();

        verifyZeroInteractions(beanFactory);
    }

    @Test
    public void initNoTemplate() {
        InjectionUtils.injectInto(null, outboundGateway, "natsTemplate");
        InjectionUtils.injectInto(null, outboundGateway, "subject");
        InjectionUtils.injectInto(subjectExpression, outboundGateway, "subjectExpression");

        outboundGateway.doInit();

        verify(beanFactory).getBean(NatsTemplate.class);
    }

    @Test
    public void handleRequestMessage() {
        String responseSubject = "responseSubject";
        String subscriptionId = "subscriptionId";
        String replyTo = "replyTo";
        when(requestMessage.getPayload()).thenAnswer(a -> requestPayload);
        when(natsTemplate.request(
                requestPayload,
                subject,
                replyPayloadType,
                requestTimeout,
                TimeUnit.MILLISECONDS)).thenReturn(natsResponse);
        when(natsResponse.getPayload()).thenReturn(responsePayload);
        when(natsResponse.getMessage()).thenReturn(natsResponseMessage);
        when(natsResponseMessage.getSubject()).thenReturn(responseSubject);
        when(natsResponseMessage.getSubscription()).thenReturn(subscription);
        when(natsResponseMessage.getSID()).thenReturn(subscriptionId);
        when(natsResponseMessage.getReplyTo()).thenReturn(replyTo);

        Message<?> returnValue = (Message<?>) outboundGateway.handleRequestMessage(requestMessage);

        assertThat(returnValue.getPayload(), is(equalTo(responsePayload)));
        assertThat(returnValue.getHeaders().get(SUBJECT, String.class), is(equalTo(responseSubject)));
        assertThat(returnValue.getHeaders().get(SUBSCRIPTION, Subscription.class), is(equalTo(subscription)));
        assertThat(returnValue.getHeaders().get(SUBSCRIPTION_ID, String.class), is(equalTo(subscriptionId)));
        assertThat(returnValue.getHeaders().get(REPLY_TO, String.class), is(equalTo(replyTo)));

    }

    @Test
    public void handleRequestMessageAsyncNoTimeout() {
        InjectionUtils.injectInto(true, outboundGateway, "async");
        InjectionUtils.injectInto(null, outboundGateway, "subject");
        InjectionUtils.injectInto(subjectExpression, outboundGateway, "subjectExpression");

        String calculatedSubject = "calculatedSubject";

        when(subjectExpression.getValue(requestMessage, String.class)).thenReturn(calculatedSubject);
        when(requestMessage.getPayload()).thenAnswer(a -> requestPayload);
        when(natsTemplate.requestAsync(requestPayload,
                calculatedSubject,
                replyPayloadType)).thenAnswer(a -> completableFuture);
        when(completableFuture.thenApplyAsync(Mockito.isA(Function.class))).thenReturn(completableFuture);

        outboundGateway.handleRequestMessage(requestMessage);

        verify(completableFuture).whenComplete(Mockito.isA(BiConsumer.class));

    }

    @Test
    public void handleRequestMessageAsyncTimeout() {
        final long anotherTimeout = 2000L;
        InjectionUtils.injectInto(anotherTimeout, outboundGateway, "requestTimeout");
        InjectionUtils.injectInto(true, outboundGateway, "async");

        when(requestMessage.getPayload()).thenAnswer(a -> requestPayload);
        when(natsTemplate.requestAsync(requestPayload,
                subject,
                replyPayloadType)).thenAnswer(a -> completableFuture);
        when(completableFuture.orTimeout(anotherTimeout, TimeUnit.MILLISECONDS)).thenReturn(completableFuture);
        when(completableFuture.thenApplyAsync(Mockito.isA(Function.class))).thenReturn(completableFuture);

        outboundGateway.handleRequestMessage(requestMessage);

        verify(completableFuture).whenComplete(Mockito.isA(BiConsumer.class));

    }

    @Test(expected = DestinationResolutionException.class)
    public void handleRequestMessageEmptySubject() {
        InjectionUtils.injectInto(null, outboundGateway, "subject");
        InjectionUtils.injectInto(subjectExpression, outboundGateway, "subjectExpression");

        when(subjectExpression.getValue(requestMessage, String.class)).thenReturn("");
        when(requestMessage.getHeaders()).thenReturn(messageHeaders);
        when(messageHeaders.getId()).thenReturn(UUID.randomUUID());

        outboundGateway.handleRequestMessage(requestMessage);
    }
}
