package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.service.UserProducer;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProducerImpl implements UserProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${tech.vtsign.kafka.user-service.register}")
    private String topicUserServiceRegister;

    @Override
    public void sendMessage(Object object) {
        Message<Object> message = MessageBuilder
                .withPayload(object)
                .setHeader(KafkaHeaders.TOPIC, topicUserServiceRegister)
                .build();
        this.kafkaTemplate.send(message);
    }

}
