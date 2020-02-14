# NATS for Spring Integration

## XML Namespace
The target namespace for NATS Spring Integration is **http://kasource.org/schema/spring-integration-nats**, which can be configured in the Spring XML configuration files as shown below.
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:nats-int="http://kasource.org/schema/spring-integration-nats"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://kasource.org/schema/spring-integration-nats http://kasource.org/schema/spring-integration-nats.xsd">

</beans>
```
## Inbound

### Inbound Channel Adapter
Minimal configuration
```
<?xml version="1.0" encoding="UTF-8"?>
<nats-int:inbound-channel-adapter channel="personChannel"
                                  request-payload-type="org.kasource.spring.nats.integration.integration.Person"
                                  subject="person-subject"/>
```

##### Attributes
| Name                  | Type    | Mandatory  | Default | Description                                                |
|:----------------------|:-------:|:----------:|:--------|:-----------------------------------------------------------|
| channel               | String  | Yes        | -       | The channel to send incoming messages to                   |
| request-payload-type  | Class   | Yes        | -       | The class name of the NATS message payload (message data)  |
| subject               | String  | Yes        | -       | The NATS subject to read messages from                     |
| queue-name            | String  | No         | -       | The name of the consumer group share messages with         |
| error-channel         | String  | No         | -       | The channel to send errors to        |
| consumer-manager      | String  | No         | Looked up by type in the ApplicationContext, if not set. | The NatsConsumerManager to use, must be found in ApplicationContext or set to a bean name |
s

### Inbound Gateway
Minimal configuration
```
<?xml version="1.0" encoding="UTF-8"?>
<nats-int:inbound-gateway request-channel="personRequestChannel"
                          request-payload-type="org.kasource.spring.nats.integration.integration.Person"
                          reply-channel="personResponseChannel"
                          subject="person-subject"/>
```

##### Attributes
| Name                  | Type    | Mandatory  | Default | Description                                                |
|:----------------------|:-------:|:----------:|:--------|:-----------------------------------------------------------|
| request-channel       | String  | Yes        | -       | The channel to send incoming messages to                   |
| request-payload-type  | Class   | Yes        | -       | The class name of the NATS message payload (message data)  |
| request-timeout       | long    | No         | -1      | The request timeout, to wait for a response in milliseconds. Negative values indicates no timeout.  |
| reply-channel         | String  | Yes        | -       | The channel to send responses to                           |
| subject               | String  | Yes        | -       | The NATS subject to read messages from                     |
| queue-name            | String  | No         | -       | The name of the consumer group share messages with         |
| error-channel         | String  | No         | -       | The channel to send errors to, mutually exclusive to error-subject. If both are set error-channel takes precedence.      |
| error-subject         | String  | No         | -       | The subject prefix to send errors to, mutually exclusive to error-channel.         |
| template              | String  | No         | Looked up by type in the ApplicationContext, if not set. | The NatsTemple to use, must be found in ApplicationContext or set to a bean name |
| consumer-manager      | String  | No         | Looked up by type in the ApplicationContext, if not set. | The NatsConsumerManager to use, must be found in ApplicationContext or set to a bean name |
| task-executor         | String  | No         | Looked up by type (ThreadPoolTaskExecutor) in the ApplicationContext, if not set the default one will be used. | The ThreadPoolTaskExecutor to use |

## Outbound

### Outbound Channel Adapter
Minimal configuration
```
<?xml version="1.0" encoding="UTF-8"?>
<nats-int:outbound-channel-adapter channel="personChannel" 
                                   subject="person-subject"/>
```
##### Attributes
| Name                  | Type    | Mandatory  | Default | Description                                                |
|:----------------------|:-------:|:----------:|:--------|:-----------------------------------------------------------|
| channel               | String  | Yes        | -       | The channel to read outgoing messages from                 |
| subject               | String  | No         | -       | The NATS subject to write messages to. Mutual exclusive with subject-expression, if both are set subject takes precedence.                      |
| subject-expression    | String  | No         | -       | SpEL expression for the NATS subject to write messages to. Mutual exclusive with subject.                  |
| template              | String  | No         | Looked up by type in the ApplicationContext, if not set. | The NatsTemple to use, must be found in ApplicationContext or set to a bean name |

### Outbound Gateway
Minimal configuration
```
<?xml version="1.0" encoding="UTF-8"?>
<nats-int:outbound-gateway request-channel="personRequestChannel"
                           reply-channel="personResponseChannel"
                           reply-payload-type="org.kasource.spring.nats.integration.integration.Person"
                           subject="person-subject"/>
```
##### Attributes
| Name                  | Type    | Mandatory  | Default | Description                                                |
|:----------------------|:-------:|:----------:|:--------|:-----------------------------------------------------------|
| request-channel       | String  | Yes        | -       | The channel to send incoming messages to                   |
| request-timeout       | long    | No         | -1      | The request timeout, to wait for a response in milliseconds. Negative values indicates no timeout.  |
| error-channel         | String  | No         | -       | The channel to send errors to                              |
| reply-channel         | String  | Yes        | -       | The channel to send responses to                           |
| reply-payload-type    | Class   | Yes        | -       | The class name of the NATS message payload (message data)  |
| subject               | String  | No         | -       | The NATS subject to write messages to. Mutual exclusive with subject-expression, if both are set subject takes precedence.                      |
| subject-expression    | String  | No         | -       | SpEL expression for the NATS subject to write messages to. Mutual exclusive with subject.                  |
| template              | String  | No         | Looked up by type in the ApplicationContext, if not set. | The NatsTemplte to use, must be found in ApplicationContext or set to a bean name |
| requires-reply        | Boolean | No         | false   | Set to true if reply is required |
| async                 | Boolean | No         | false   | Set to true to allow response to be handled asynchronously |

                            