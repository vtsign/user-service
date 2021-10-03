package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import tech.vtsign.userservice.domain.User;
import tech.vtsign.userservice.service.UserProducer;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProducerImpl implements UserProducer {

    private final KafkaTemplate<String, User> kafkaTemplate;

    @Override
    public void sendMessage(String topic, User user) {
        log.info(String.format("#### -> Producing message -> %s", user));

        ListenableFuture<SendResult<String, User>> future =
                kafkaTemplate.send(topic, user);

        future.addCallback(new ListenableFutureCallback<SendResult<String, User>>() {

            @Override
            public void onSuccess(SendResult<String, User> result) {
                log.info("message sent to", user.getEmail());
            }
            @Override
            public void onFailure(Throwable ex) {
                log.error("sent message error {}", ex);
            }
        });
    }
}
