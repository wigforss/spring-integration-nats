package org.kasource.spring.nats.integration.config.xmlns;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import org.kasource.spring.nats.integration.config.xmlns.parser.InboundChannelAdapterBeanDefinitionParser;
import org.kasource.spring.nats.integration.config.xmlns.parser.InboundGatewayBeanDefinitionParser;
import org.kasource.spring.nats.integration.config.xmlns.parser.OutboundChannelAdapterBeanDefinitionParser;
import org.kasource.spring.nats.integration.config.xmlns.parser.OutboundGatewayBeanDefinitionParser;

@SuppressWarnings("checkstyle:classdataabstractioncoupling")
public class IntegrationNatsXmlNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("inbound-gateway", new InboundGatewayBeanDefinitionParser());
        registerBeanDefinitionParser("inbound-channel-adapter", new InboundChannelAdapterBeanDefinitionParser());
        registerBeanDefinitionParser("outbound-gateway", new OutboundGatewayBeanDefinitionParser());
        registerBeanDefinitionParser("outbound-channel-adapter", new OutboundChannelAdapterBeanDefinitionParser());
    }
}
