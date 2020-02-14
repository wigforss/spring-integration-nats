package org.kasource.spring.nats.integration.integration;

public class PersonService {

    private long delayMillis = 0;

    public Person onPerson(Person person) {
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Person transformed = new Person();
        transformed.setAge(person.getAge() + 1);
        transformed.setName(person.getName());
        return transformed;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }
}
