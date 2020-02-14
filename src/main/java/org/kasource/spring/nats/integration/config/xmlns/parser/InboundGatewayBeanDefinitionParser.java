package org.kasource.spring.nats.integration.config.xmlns.parser;

import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.integration.config.xml.AbstractInboundGatewayParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;

import org.kasource.spring.nats.integration.inbound.NatsInboundGateway;

import org.w3c.dom.Element;

public class InboundGatewayBeanDefinitionParser extends AbstractInboundGatewayParser {

    private Set<String> nonEligableAttributes = Set.of(
            "request-payload-type",
            "subject",
            "queue-name",
            "template",
            "consumer-manager",
            "error-on-timeout",
            "task-executor"
    );

    @Override
    protected Class<?> getBeanClass(Element element) {
        return NatsInboundGateway.class;
    }

    @Override
    protected boolean isEligibleAttribute(String attributeName) {
        return !nonEligableAttributes.contains(attributeName) && super.isEligibleAttribute(attributeName);
    }

    @Override
    protected void doPostProcess(BeanDefinitionBuilder builder, Element element) {
        builder.addConstructorArgValue("true".equals(element.getAttribute("error-on-timeout")));
        builder.addPropertyValue("requestPayloadType", element.getAttribute("request-payload-type"));
        builder.addPropertyValue("subject", element.getAttribute("subject"));
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "queue-name");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "template", "natsTemplate");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "consumer-manager", "natsConsumerManager");

        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "task-executor");
    }
}
