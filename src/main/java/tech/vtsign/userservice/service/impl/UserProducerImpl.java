package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public void sendMessage(Object object, String topic) {
        log.info("sendMessage to: {}", topic);
        Message<Object> message = MessageBuilder
                .withPayload(object)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        this.kafkaTemplate.send(message);
    }

}
