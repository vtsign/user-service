package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.domain.Permission;
import tech.vtsign.userservice.repository.PermissionRepository;
import tech.vtsign.userservice.service.PermissionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository repository;

    @Override
    public List<Permission> findAll() {
        return repository.findAll();
    }

    @Override
    public <S extends Permission> S save(S s) {
        return repository.save(s);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void delete(Permission permission) {
        repository.delete(permission);
    }
}
