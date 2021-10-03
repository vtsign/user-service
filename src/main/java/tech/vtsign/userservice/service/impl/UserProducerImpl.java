package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.service.UserProducer;

//public class UserProducerImpl implements UserProducer {
//
//    private final KafkaTemplate<String, User> kafkaTemplate;
//
//    @Override
//    public void sendMessage(String topic, User user) {
//        log.info(String.format("#### -> Producing message -> %s", user));
//
//        ListenableFuture<SendResult<String, User>> future =
//                kafkaTemplate.send(topic, user);
//
//        future.addCallback(new ListenableFutureCallback<SendResult<String, User>>() {
//
//            @Override
//            public void onSuccess(SendResult<String, User> result) {
//                log.info("message sent to", user.getEmail());
//            }
//            @Override
//            public void onFailure(Throwable ex) {
//                log.error("sent message error {}", ex);
//            }
//        });
//    }
//}


@Service
@Slf4j
@RequiredArgsConstructor
public class UserProducerImpl implements UserProducer {
    private static final String TOPIC = "final-topic";


    private final KafkaTemplate<String, User> kafkaTemplate;

    @Override
    public void sendMessage(User user) {
        Message<User> message = MessageBuilder
                .withPayload(user)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .build();
        this.kafkaTemplate.send(message);
    }

}
