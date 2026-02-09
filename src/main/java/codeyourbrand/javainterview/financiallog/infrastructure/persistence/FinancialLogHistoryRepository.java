package codeyourbrand.javainterview.financiallog.infrastructure.persistence;

import codeyourbrand.javainterview.financiallog.domain.FinancialLogHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FinancialLogHistoryRepository extends JpaRepository<FinancialLogHistory, UUID> {
    @Query("SELECT f FROM FinancialLogHistory f WHERE f.financialLogEntry.uuid = :uuid ORDER BY f.createdAt DESC")
    List<FinancialLogHistory> findByFinancialLogUuidOrderByCreatedAtDesc(UUID uuid);
}
