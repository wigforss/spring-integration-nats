package org.kasource.spring.nats.integration.config.xmlns.parser;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.AbstractChannelAdapterParser;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;

import org.kasource.spring.nats.integration.inbound.NatsInboundChannelAdapter;

import org.w3c.dom.Element;

public class InboundChannelAdapterBeanDefinitionParser extends AbstractChannelAdapterParser {

    @Override
    protected AbstractBeanDefinition doParse(Element element, ParserContext parserContext, String channelName) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(NatsInboundChannelAdapter.class);

        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "error-channel");
        IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "channel", "outputChannel");
        builder.addPropertyValue("requestPayloadType", element.getAttribute("request-payload-type"));
        builder.addPropertyValue("subject", element.getAttribute("subject"));
        builder.addPropertyValue("queueName", element.getAttribute("queue-name"));
        IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "consumer-manager", "natsConsumerManager");


        return builder.getBeanDefinition();
    }

}
