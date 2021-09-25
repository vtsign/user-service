package tech.vtsign.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.userservice.domain.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
