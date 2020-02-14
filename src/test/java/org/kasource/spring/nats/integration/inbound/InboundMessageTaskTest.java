package org.kasource.spring.nats.integration.inbound;

import org.springframework.messaging.Message;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InboundMessageTaskTest {
    @Mock
    private NatsInboundGateway inboundGateway;

    @Mock
    private Message<?> requestMessage;

    @Mock
    private Message<?> responseMessage;


    private InboundMessageTask task;

    @Before
    public void setup() {
        task = new InboundMessageTask(inboundGateway, requestMessage);
    }

    @Test
    public void call() throws Exception {
        when(inboundGateway.sendAndReceiveMessage(requestMessage)).thenAnswer(a -> responseMessage);

        Message<?> returnValue = task.call();

        assertThat(returnValue, is(equalTo(responseMessage)));
    }
}
