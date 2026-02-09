package codeyourbrand.javainterview.financiallog.infrastructure.persistence;

import codeyourbrand.javainterview.financiallog.application.FinancialLogApi;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogEntry;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogHistory;
import codeyourbrand.javainterview.financiallog.domain.Reference;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository("newFinancialLogRepository")
@RequiredArgsConstructor
public class FinancialLogRepository {
    private final FinancialLogEntryRepository financialLogEntryRepository;
    private final CalculateFinancialLogTotals calculateFinancialLogTotals;
    private final FinancialLogHistoryRepository financialLogHistoryRepository;
    private final EntityManager entityManager;

    public List<UUID> saveAll(List<FinancialLogEntry> entries) {
        List<FinancialLogEntry> saved = financialLogEntryRepository.saveAll(entries);
        return saved.stream().map(FinancialLogEntry::toUuidOnly).toList();
    }

    public List<FinancialLogEntry> findAllByUuids(@NonNull Set<UUID> uuids) {
        return financialLogEntryRepository.findAllByUuidIn(uuids);
    }

    public void createHistory(Map<UUID, FinancialLogHistory.Snapshot> entryUuidToOldSnapshot) {
        List<FinancialLogHistory> historyList =
                financialLogEntryRepository.findAllByUuidIn(entryUuidToOldSnapshot.keySet()).stream()
                        .map(entry -> FinancialLogHistory.create(entry, entryUuidToOldSnapshot.get(entry.toUuidOnly())))
                        .toList();
        financialLogHistoryRepository.saveAll(historyList);
    }

    public PageWithTotals findPageWithTotals(FinancialLogApi.GetFinancialLogFiltersRequest request) {
        FinancialLogEntrySpecification financialLogEntrySpecification = new FinancialLogEntrySpecification(request);
        CalculateFinancialLogTotals.IncomeCostAndProfit incomeCostAndProfit =
                calculateFinancialLogTotals.calculateIncomeCostAndProfit(financialLogEntrySpecification);

        Page<FinancialLogEntry> page = getFinancialLogPage(request, financialLogEntrySpecification);

        return new PageWithTotals(
                page, incomeCostAndProfit.income(), incomeCostAndProfit.cost(), incomeCostAndProfit.profit());
    }

    private Page<FinancialLogEntry> getFinancialLogPage(
            FinancialLogApi.GetFinancialLogFiltersRequest request, FinancialLogEntrySpecification financialLogEntrySpecification) {
        Sort sort = Sort.by(request.getSortDirection(), request.getSortBy().getValue());
        return financialLogEntryRepository.findAll(financialLogEntrySpecification, request.getPageRequest(sort));
    }

    public void acceptDrafts(List<UUID> uuids) {
        financialLogEntryRepository.changeStatus(FinancialLogApi.Status.ACCEPTED, uuids);
    }

    public void delete(List<UUID> uuids) {
        financialLogEntryRepository.deleteByUuidIn(uuids);
    }

    /**
     * Saves the given FinancialLogEntry and refreshes it from the database to ensure the sequence number has been fetched.
     * <p>
     * The reason to use entityManager.refresh is that the sequence number is generated in the database upon insert,
     * that's why we need to refresh the entity after saving it otherwise the sequence number will be always null for newly saved entity.
     *
     * @param logEntry new FinancialLogEntry to save
     * @return the saved FinancialLogEntry with updated fields
     */
    public FinancialLogEntry save(FinancialLogEntry logEntry) {
        FinancialLogEntry saved = financialLogEntryRepository.saveAndFlush(logEntry);
        entityManager.refresh(saved);
        return saved;
    }

    public Pair<FinancialLogEntry, List<FinancialLogHistory>> findByUuidWithHistory(UUID uuid) {
        FinancialLogEntry financialLogEntry =
                financialLogEntryRepository.findById(uuid).orElseThrow(NoSuchElementException::new);
        List<FinancialLogHistory> history =
                financialLogHistoryRepository.findByFinancialLogUuidOrderByCreatedAtDesc(uuid);

        return Pair.of(financialLogEntry, history);
    }

    public FinancialLogEntry findByUuid(UUID uuid) {
        return financialLogEntryRepository.findById(uuid).orElseThrow(NoSuchElementException::new);
    }

    public List<FinancialLogHistory> findHistoriesByFinancialLogUuids(@NonNull UUID uuids) {
        return financialLogHistoryRepository.findByFinancialLogUuidOrderByCreatedAtDesc(uuids);
    }

    public List<FinancialLogEntry> findFinancialLogsByReferences(@NonNull Set<FinancialLogApi.Reference> references) {
        return financialLogEntryRepository.findByReferences(mapToReferenceRepositoryType(references));
    }

    private static Set<Reference> mapToReferenceRepositoryType(Set<FinancialLogApi.Reference> references) {
        return references.stream()
                .map(ref -> new Reference(ref.id(), ref.type(), ref.businessId()))
                .collect(Collectors.toSet());
    }

    public record PageWithTotals(Page<FinancialLogEntry> page, BigDecimal income, BigDecimal cost, BigDecimal profit) {}

    public FinancialLogApi.DailySummaryResponse getDailySummary(FinancialLogApi.GetFinancialLogFiltersRequest request) {
        var specification = new FinancialLogEntrySpecification(request);
        var summary = calculateFinancialLogTotals.calculateFinancialSummary(specification);
        return new FinancialLogApi.DailySummaryResponse(summary.revenue(), summary.profit(), summary.orders());
    }
}
