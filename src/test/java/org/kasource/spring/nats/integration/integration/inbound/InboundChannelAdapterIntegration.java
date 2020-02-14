package org.kasource.spring.nats.integration.integration.inbound;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.kasource.spring.nats.NatsTemplate;
import org.kasource.spring.nats.integration.integration.Person;
import org.kasource.spring.nats.integration.integration.SpringMessageListener;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:integration/spring/inbound-channel-adapter.xml")
@DirtiesContext
public class InboundChannelAdapterIntegration {



    @Autowired
    private NatsTemplate template;

    @Autowired
    private SpringMessageListener<Person> personListener;


    @Test
    public void inboundChannelAdapter() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        personListener.setLatch(countDownLatch);
        String name = "name";
        final int age = 1;

        Person person = new Person();
        person.setName(name);
        person.setAge(age);

        template.publish(person, "person-subject");

        boolean messageReceived = countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertThat("No message received within timeout", messageReceived, is(true));

        assertThat(personListener.getPayload().isPresent(), is(true));
        assertThat(personListener.getPayload().get().getAge(), is(age));
        assertThat(personListener.getPayload().get().getName(), is(equalTo(name)));

    }



}
