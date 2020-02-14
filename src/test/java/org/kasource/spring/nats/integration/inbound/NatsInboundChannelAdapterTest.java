package org.kasource.spring.nats.integration.inbound;

import java.util.function.BiConsumer;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.SmartLifecycle;

import static org.kasource.spring.nats.integration.NatsHeaders.QUEUE;
import static org.kasource.spring.nats.integration.NatsHeaders.REPLY_TO;
import static org.kasource.spring.nats.integration.NatsHeaders.SUBJECT;
import static org.kasource.spring.nats.integration.NatsHeaders.SUBSCRIPTION;
import static org.kasource.spring.nats.integration.NatsHeaders.SUBSCRIPTION_ID;
import org.kasource.spring.nats.consumer.NatsConsumerManager;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.inject.util.InjectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class NatsInboundChannelAdapterTest {
    @Mock
    private NatsConsumerManager natsConsumerManager;

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private SmartLifecycle dispatcher;

    @Mock
    private Message natsMessage;

    @Mock
    private Object payload;

    @Mock
    private Subscription subscription;

    private Class<String> requestPayloadType = String.class;
    private String subject = "subject";
    private String queueName = "queueName";

    @InjectMocks
    private NatsInboundChannelAdapter inboundAdapter;


    @Before
    public void setup() {
        inboundAdapter.setRequestPayloadType(requestPayloadType);
        inboundAdapter.setNatsConsumerManager(natsConsumerManager);
        inboundAdapter.setSubject(subject);
        inboundAdapter.setQueueName(queueName);
        InjectionUtils.injectInto(dispatcher, inboundAdapter, "dispatcher");
    }

    @Test
    public void initNoConsumerManager() {

        InjectionUtils.injectInto(null, inboundAdapter, "natsConsumerManager");
        InjectionUtils.injectInto(beanFactory, inboundAdapter, "beanFactory");

        when(beanFactory.getBean(NatsConsumerManager.class)).thenReturn(natsConsumerManager);


        inboundAdapter.onInit();

        verify(natsConsumerManager).register(
                Mockito.isA(BiConsumer.class),
                eq(requestPayloadType),
                eq(subject),
                eq(queueName));


    }

    @Test
    public void initWithConsumerManager() {

        InjectionUtils.injectInto(beanFactory, inboundAdapter, "beanFactory");

        inboundAdapter.onInit();

        verify(beanFactory, times(0)).getBean(NatsConsumerManager.class);

        verify(natsConsumerManager).register(
                Mockito.isA(BiConsumer.class),
                eq(requestPayloadType),
                eq(subject),
                eq(queueName));


    }

    @Test
    public void start() {
        inboundAdapter.doStart();

        verify(dispatcher).start();
    }

    @Test
    public void stop() {
        inboundAdapter.doStop();

        verify(dispatcher).stop();
    }

    @Test
    public void buildMessage() {
        String subject = "subject";
        String subscriptionId = "subscriptionId";
        String replyTo = "replyTo";

        when(natsMessage.getSubject()).thenReturn(subject);
        when(natsMessage.getSubscription()).thenReturn(subscription);
        when(natsMessage.getSID()).thenReturn(subscriptionId);
        when(natsMessage.getReplyTo()).thenReturn(replyTo);

        org.springframework.messaging.Message<Object> response = inboundAdapter.buildMessage(payload, natsMessage);

        assertThat(response.getPayload(), is(equalTo(payload)));
        assertThat(response.getHeaders().get(SUBJECT, String.class), is(equalTo(subject)));
        assertThat(response.getHeaders().get(SUBSCRIPTION_ID, String.class), is(equalTo(subscriptionId)));
        assertThat(response.getHeaders().get(REPLY_TO, String.class), is(equalTo(replyTo)));
        assertThat(response.getHeaders().get(SUBSCRIPTION, Subscription.class), is(equalTo(subscription)));
        assertThat(response.getHeaders().get(QUEUE, String.class), is(equalTo(queueName)));
    }
}
