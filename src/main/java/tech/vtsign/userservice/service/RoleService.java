package tech.vtsign.userservice.service;

import tech.vtsign.userservice.domain.Role;

import java.util.List;

public interface RoleService {
    List<Role> findAll();

    <S extends Role> S save(S s);

    long count();


    void delete(Role role);
}
