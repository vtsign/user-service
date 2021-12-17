package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.repository.RoleRepository;
import tech.vtsign.userservice.service.RoleService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repository;

    @Override
    public List<Role> findAll() {
        return repository.findAll();
    }

    @Override
    public <S extends Role> S save(S s) {
        return repository.save(s);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void delete(Role role) {
        repository.delete(role);
    }

    @Override
    public Role findByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }
}
