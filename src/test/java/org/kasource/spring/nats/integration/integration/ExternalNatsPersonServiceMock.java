package org.kasource.spring.nats.integration.integration;

import org.kasource.spring.nats.NatsTemplate;
import org.kasource.spring.nats.annotation.Consumer;

import io.nats.client.Message;

public class ExternalNatsPersonServiceMock {

    private PersonService personService;
    private NatsTemplate template;

    @Consumer(subject = "person-subject")
    public void onPerson(Person person, Message message) {
        template.publish(personService.onPerson(person), message.getReplyTo());
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setTemplate(NatsTemplate template) {
        this.template = template;
    }
}
