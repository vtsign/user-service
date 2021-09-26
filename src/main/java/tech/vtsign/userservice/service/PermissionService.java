package tech.vtsign.userservice.service;

import tech.vtsign.userservice.domain.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionService {
    List<Permission> findAll();

    Permission getById(Long aLong);

    <S extends Permission> S save(S s);

    Optional<Permission> findById(Long aLong);

    boolean existsById(Long aLong);

    long count();

    void deleteById(Long aLong);

    void delete(Permission permission);
}
