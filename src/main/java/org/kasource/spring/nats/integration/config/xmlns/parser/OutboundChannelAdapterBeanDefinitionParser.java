package org.kasource.spring.nats.integration.config.xmlns.parser;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractOutboundChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;

import org.kasource.spring.nats.integration.outbound.NatsMessageHandler;

import org.w3c.dom.Element;

public class OutboundChannelAdapterBeanDefinitionParser extends AbstractOutboundChannelAdapterParser {


    @Override
    protected AbstractBeanDefinition parseConsumer(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(NatsMessageHandler.class);

        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "subject");
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "subject-expression");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "template", "natsTemplate");
        return builder.getBeanDefinition();
    }
}
