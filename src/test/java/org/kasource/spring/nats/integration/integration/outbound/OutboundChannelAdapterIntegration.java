package org.kasource.spring.nats.integration.integration.outbound;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.kasource.spring.nats.consumer.NatsConsumerManager;
import org.kasource.spring.nats.integration.integration.NatsListener;
import org.kasource.spring.nats.integration.integration.Person;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:integration/spring/outbound-channel-adapter.xml")
@DirtiesContext
public class OutboundChannelAdapterIntegration {


    @Autowired
    private NatsConsumerManager manager;

    @Autowired
    private MessageChannel personChannel;

    private NatsListener<Person> personListener = new NatsListener<>();


    @Test
    public void outboundChannelAdapter() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        personListener.setLatch(countDownLatch);
        String name = "name";
        final int age = 1;

        Person person = new Person();
        person.setName(name);
        person.setAge(age);


        manager.register(personListener, Person.class, "person-subject");

        personChannel.send(MessageBuilder.withPayload(person).build());

        boolean messageReceived = countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat("No message received within timeout", messageReceived, is(true));

        assertThat(personListener.getObject().isPresent(), is(true));
        assertThat(personListener.getObject().get().getAge(), is(age));
        assertThat(personListener.getObject().get().getName(), is(equalTo(name)));

    }




}
