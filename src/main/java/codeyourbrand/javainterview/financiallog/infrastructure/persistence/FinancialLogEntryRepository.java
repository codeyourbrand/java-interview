package codeyourbrand.javainterview.financiallog.infrastructure.persistence;

import codeyourbrand.javainterview.financiallog.application.FinancialLogApi;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogEntry;
import codeyourbrand.javainterview.financiallog.domain.Reference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FinancialLogEntryRepository
        extends JpaRepository<FinancialLogEntry, UUID>, JpaSpecificationExecutor<FinancialLogEntry> {

    @Query("SELECT f FROM FinancialLogEntry f WHERE f.uuid IN :uuids")
    List<FinancialLogEntry> findAllByUuidIn(@Param("uuids") Set<UUID> uuids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
			UPDATE FinancialLogEntry f
				SET f.status = :status
			WHERE f.uuid in :uuids
	""")
    void changeStatus(@Param("status") FinancialLogApi.Status status, @Param("uuids") List<UUID> uuids);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM FinancialLogEntry f WHERE f.uuid IN :uuids")
    void deleteByUuidIn(@Param("uuids") List<UUID> uuids);

    @Query("""
	SELECT f FROM FinancialLogEntry f WHERE f.reference IN :references""")
    List<FinancialLogEntry> findByReferences(@Param("references") Set<Reference> references);
}
