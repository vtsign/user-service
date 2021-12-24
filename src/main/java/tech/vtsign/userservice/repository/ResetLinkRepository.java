package tech.vtsign.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.vtsign.userservice.domain.ResetLink;

import java.util.UUID;

@Repository
public interface ResetLinkRepository extends JpaRepository<ResetLink, UUID> {
}
