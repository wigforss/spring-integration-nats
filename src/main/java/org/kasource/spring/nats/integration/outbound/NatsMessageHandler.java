package org.kasource.spring.nats.integration.outbound;

import org.springframework.expression.Expression;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.core.DestinationResolutionException;
import org.springframework.util.Assert;

import org.kasource.spring.nats.NatsTemplate;

import org.apache.commons.lang.StringUtils;

public class NatsMessageHandler extends AbstractMessageHandler {

    private NatsTemplate natsTemplate;
    private String subject;
    private Expression subjectExpression;

    @Override
    public void onInit() {
        super.onInit();
        Assert.isTrue(!StringUtils.isEmpty(subject) || subjectExpression != null, "either subject or subject-expression must be set");
        if (natsTemplate == null) {
            natsTemplate = this.getBeanFactory().getBean(NatsTemplate.class);
        }
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        String recipientSubject = subject;

        if (recipientSubject == null) {
            recipientSubject = subjectExpression.getValue(message, String.class);
        }
        if (!StringUtils.isEmpty(recipientSubject)) {
            natsTemplate.publish(message.getPayload(), recipientSubject);
        } else {
            throw new DestinationResolutionException("Can't resolve subject for outbound message: " + message.getHeaders().getId());
        }

    }


    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setSubjectExpression(Expression subjectExpression) {
        this.subjectExpression = subjectExpression;
    }

    public void setNatsTemplate(NatsTemplate natsTemplate) {
        this.natsTemplate = natsTemplate;
    }

}
