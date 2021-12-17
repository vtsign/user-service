package tech.vtsign.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import tech.vtsign.userservice.domain.TransactionMoney;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionMoneyRepository extends JpaRepository<TransactionMoney, UUID>, JpaSpecificationExecutor<TransactionMoney> {
    @Query(value = "select sum(amount) from TransactionMoney where status = ?1 and createdDate between ?2 and ?3")
    Long getSumAmountByStatus(String status, LocalDateTime fromDate, LocalDateTime toDate);
}
