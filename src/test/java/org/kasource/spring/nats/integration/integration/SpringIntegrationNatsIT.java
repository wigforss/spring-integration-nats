package org.kasource.spring.nats.integration.integration;

import java.io.File;

import org.springframework.util.SocketUtils;

import org.kasource.spring.nats.integration.integration.inbound.InboundChannelAdapterIntegration;
import org.kasource.spring.nats.integration.integration.inbound.InboundGatewayIntegration;
import org.kasource.spring.nats.integration.integration.inbound.InboundOutboundGatewayIntegration;
import org.kasource.spring.nats.integration.integration.outbound.OutboundChannelAdapterIntegration;
import org.kasource.spring.nats.integration.integration.outbound.OutboundGatewayIntegration;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;


@RunWith(Suite.class)
@Suite.SuiteClasses({

                            InboundChannelAdapterIntegration.class,
                            InboundGatewayIntegration.class,
                            InboundOutboundGatewayIntegration.class,
                            OutboundChannelAdapterIntegration.class,
                            OutboundGatewayIntegration.class
                    })
public class SpringIntegrationNatsIT {
    public static final Integer NATS_PORT = SocketUtils.findAvailableTcpPort(10000);
    public static final Integer NATS_MONITOR_PORT = SocketUtils.findAvailableTcpPort(10000);


    @ClassRule
    public static DockerComposeContainer dockerComposeContainer =
            new DockerComposeContainer(new File("src/test/resources/integration/docker/docker-compose.yml"))
                    .withEnv("NATS_PORT", NATS_PORT.toString())
                    .withEnv("NATS_MONITOR_PORT", NATS_MONITOR_PORT.toString())
                    .waitingFor("nats", Wait.forListeningPort())
                    .waitingFor("nats", Wait.forLogMessage(".*Server is ready.*\\n", 1));

    @BeforeClass
    public static void setupPorts() {
        System.setProperty("NATS_PORT", NATS_PORT.toString());
        System.setProperty("NATS_MONITOR_PORT", NATS_MONITOR_PORT.toString());
        System.out.println("\n\n##################################");
        System.out.println("Starting NATS with port " + NATS_PORT + " and monitoring port " + NATS_MONITOR_PORT);
        System.out.println("##################################\n\n");
    }

}
