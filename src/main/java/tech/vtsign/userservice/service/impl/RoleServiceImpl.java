package tech.vtsign.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tech.vtsign.userservice.constant.RoleName;
import tech.vtsign.userservice.domain.Role;
import tech.vtsign.userservice.exception.NotFoundException;
import tech.vtsign.userservice.repository.RoleRepository;
import tech.vtsign.userservice.service.RoleService;

import java.util.List;
import java.util.Optional;

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
        Optional<Role> roleOpt = repository.findByName(name);
        if (roleOpt.isPresent()) {
            return roleOpt.get();
        } else if (RoleName.ROLE_USER.equals(name) || RoleName.ROLE_ADMIN.equals(name)) {
            return repository.save(new Role(name));
        } else {
            throw new NotFoundException("Role not found");
        }
    }
}
