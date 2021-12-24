package tech.vtsign.userservice.service;


public interface UserProducer {
    void sendMessage(Object message, String topic);
}
