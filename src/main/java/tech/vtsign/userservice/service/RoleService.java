package tech.vtsign.userservice.service;

import tech.vtsign.userservice.domain.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    Optional<Role> findById(long id);

    List<Role> findAll();

    <S extends Role> S save(S s);

    Optional<Role> findById(Long aLong);

    boolean existsById(Long aLong);

    long count();

    void deleteById(Long aLong);

    void delete(Role role);
}
