package org.kasource.spring.nats.integration.integration.inbound;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import org.kasource.spring.nats.NatsTemplate;
import org.kasource.spring.nats.integration.integration.Person;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:integration/spring/inbound-gateway.xml")
@DirtiesContext
public class InboundGatewayIntegration {


    @Autowired
    private NatsTemplate template;


    @Test
    public void inboundGateway() {

        String name = "name";
        final int age = 1;

        Person person = new Person();
        person.setName(name);
        person.setAge(age);

        Person response = template.requestForObject(person, "person-subject", Person.class, 2000, TimeUnit.MILLISECONDS);

        assertThat(response.getAge(), is(age + 1));
        assertThat(response.getName(), is(equalTo(name)));

    }



}
