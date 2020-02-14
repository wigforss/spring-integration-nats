package org.kasource.spring.nats.integration.config.xmlns.parser;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractConsumerEndpointParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;

import org.kasource.spring.nats.integration.outbound.NatsOutboundGateway;

import org.w3c.dom.Element;

public class OutboundGatewayBeanDefinitionParser extends AbstractConsumerEndpointParser {

    @Override
    protected BeanDefinitionBuilder parseHandler(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(NatsOutboundGateway.class);
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "subject");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "subject-expression");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "template", "natsTemplate");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "reply-timeout");
        builder.addPropertyValue("replyPayloadType", element.getAttribute("reply-payload-type"));
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "async");

        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "request-timeout");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "reply-channel", "outputChannel");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "requires-reply");
        return builder;
    }

    @Override
    protected String getInputChannelAttributeName() {
        return "request-channel";
    }
}
