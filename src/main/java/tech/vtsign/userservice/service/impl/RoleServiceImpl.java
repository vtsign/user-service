package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.repository.RoleRepository;
import tech.vtsign.userservice.service.RoleService;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepository repository;

    @Override
    public Optional<Role> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public List<Role> findAll() {
        return repository.findAll();
    }

    @Override
    public <S extends Role> S save(S s) {
        return repository.save(s);
    }

    @Override
    public Optional<Role> findById(Long aLong) {
        return repository.findById(aLong);
    }

    @Override
    public boolean existsById(Long aLong) {
        return repository.existsById(aLong);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void deleteById(Long aLong) {
        repository.deleteById(aLong);
    }

    @Override
    public void delete(Role role) {
        repository.delete(role);
    }
}
