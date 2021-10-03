package tech.vtsign.userservice.service;


import tech.vtsign.userservice.domain.User;

public interface UserProducer  {
    void sendMessage(String topic, User user);
}
