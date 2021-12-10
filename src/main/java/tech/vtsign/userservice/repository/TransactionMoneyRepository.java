package tech.vtsign.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.vtsign.userservice.domain.TransactionMoney;

import java.util.UUID;

public interface TransactionMoneyRepository extends JpaRepository<TransactionMoney, UUID> {
}
