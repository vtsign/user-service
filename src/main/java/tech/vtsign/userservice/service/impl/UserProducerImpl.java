package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.service.UserProducer;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProducerImpl implements UserProducer {

    @Value("${tech.vtsign.kafka.user-service.register}")
    private String topicUserServiceRegister;

    private final KafkaTemplate<String, User> kafkaTemplate;

    @Override
    public void sendMessage(User user) {
        Message<User> message = MessageBuilder
                .withPayload(user)
                .setHeader(KafkaHeaders.TOPIC, topicUserServiceRegister)
                .build();
        this.kafkaTemplate.send(message);
    }

}
