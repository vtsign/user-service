package tech.vtsign.userservice.service;

import tech.vtsign.userservice.domain.Permission;

import java.util.List;

public interface PermissionService {
    List<Permission> findAll();

    <S extends Permission> S save(S s);

    long count();

    void delete(Permission permission);
}
