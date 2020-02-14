package org.kasource.spring.nats.integration;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class NatsHeaders {

    public static final String REPLY_TO = "nats-reply-to";
    public static final String SUBJECT = "nats-subject";
    public static final String QUEUE = "nats-queue";
    public static final String SUBSCRIPTION = "nats-subscription";
    public static final String SUBSCRIPTION_ID = "nats-subscription-id";

    private NatsHeaders() {
    }
}
