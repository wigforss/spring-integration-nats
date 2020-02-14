package org.kasource.spring.nats.integration.outbound;

import java.util.UUID;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.Expression;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.core.DestinationResolutionException;

import org.kasource.spring.nats.NatsTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.unitils.inject.util.InjectionUtils;

@RunWith(MockitoJUnitRunner.class)
public class NatsMessageHandlerTest {
    @Mock
    private NatsTemplate natsTemplate;
    @Mock
    private Expression subjectExpression;
    @Mock
    private BeanFactory beanFactory;
    @Mock
    private Message<?> message;
    @Mock
    private Object payload;
    @Mock
    private MessageHeaders messageHeaders;

    private String subject = "subject";

    private NatsMessageHandler messageHandler = new NatsMessageHandler();

    @Before
    public void setup() {
        messageHandler.setNatsTemplate(natsTemplate);
        messageHandler.setSubject(subject);
    }

    @Test
    public void init() {
        InjectionUtils.injectInto(beanFactory, messageHandler, "beanFactory");

        messageHandler.onInit();

        verifyZeroInteractions(beanFactory);
    }

    @Test
    public void initLookup() {
        InjectionUtils.injectInto(beanFactory, messageHandler, "beanFactory");
        InjectionUtils.injectInto(null, messageHandler, "subject");
        InjectionUtils.injectInto(subjectExpression, messageHandler, "subjectExpression");
        InjectionUtils.injectInto(null, messageHandler, "natsTemplate");

        messageHandler.onInit();

        verify(beanFactory).getBean(NatsTemplate.class);
    }

    @Test
    public void handleMessageInternal() {

        when(message.getPayload()).thenAnswer(a -> payload);

        messageHandler.handleMessageInternal(message);

        verify(natsTemplate).publish(payload, subject);
    }

    @Test
    public void handleMessageInternalWithSubjectExpression() {
        String calculatedSubject = "calculatedSubject";

        InjectionUtils.injectInto(null, messageHandler, "subject");
        InjectionUtils.injectInto(subjectExpression, messageHandler, "subjectExpression");

        when(message.getPayload()).thenAnswer(a -> payload);
        when(subjectExpression.getValue(message, String.class)).thenReturn(calculatedSubject);

        messageHandler.handleMessageInternal(message);

        verify(natsTemplate).publish(payload, calculatedSubject);
    }

    @Test(expected = DestinationResolutionException.class)
    public void handleMessageInternalWithEmptySubject() {

        InjectionUtils.injectInto(null, messageHandler, "subject");
        InjectionUtils.injectInto(subjectExpression, messageHandler, "subjectExpression");

        when(subjectExpression.getValue(message, String.class)).thenReturn("");
        when(message.getHeaders()).thenReturn(messageHeaders);
        when(messageHeaders.getId()).thenReturn(UUID.randomUUID());

        messageHandler.handleMessageInternal(message);


    }

}
