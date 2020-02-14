package org.kasource.spring.nats.integration.integration.outbound;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.kasource.spring.nats.integration.integration.Person;
import org.kasource.spring.nats.integration.integration.SpringMessageListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:integration/spring/outbound-gateway.xml")
@DirtiesContext
public class OutboundGatewayIntegration {


    @Autowired
    private MessageChannel personRequestChannel;

    @Autowired
    private MessageChannel errorChannel;

    @Autowired
    private SpringMessageListener<Person> personListener;

    @Test
    public void outboundGateway() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        personListener.setLatch(countDownLatch);
        String name = "name";
        final int age = 1;

        Person person = new Person();
        person.setName(name);
        person.setAge(age);

        personRequestChannel.send(MessageBuilder.withPayload(person).setErrorChannel(errorChannel).build());

        boolean messageReceived = countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat("No message received within timeout", messageReceived, is(true));
        assertThat(personListener.getPayload().isPresent(), is(true));
        assertThat(personListener.getPayload().get().getAge(), is(age + 1));
        assertThat(personListener.getPayload().get().getName(), is(equalTo(name)));
    }



}
