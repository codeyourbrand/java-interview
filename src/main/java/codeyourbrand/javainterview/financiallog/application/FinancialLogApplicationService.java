package codeyourbrand.javainterview.financiallog.application;


import codeyourbrand.javainterview.common.annotations.ApplicationService;
import codeyourbrand.javainterview.common.messaging.MessagePublisher;
import codeyourbrand.javainterview.common.utils.CollectionUtils;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogDomainService;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogEntry;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogHistory;
import codeyourbrand.javainterview.financiallog.infrastructure.persistence.FinancialLogRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationService
public class FinancialLogApplicationService {
    private final FinancialLogRepository financialLogRepository;
    private final FinancialLogDomainService financialLogDomainService;
    private final MessagePublisher messagePublisher;

    public FinancialLogApplicationService(
            @Qualifier("newFinancialLogRepository") FinancialLogRepository financialLogRepository,
            FinancialLogDomainService financialLogDomainService,
            MessagePublisher messagePublisher) {
        this.financialLogRepository = financialLogRepository;
        this.financialLogDomainService = financialLogDomainService;
        this.messagePublisher = messagePublisher;
    }

    @Transactional
    public FinancialLogApi.FinancialLogListItemResponse createManualLog(
            @NonNull FinancialLogApi.CreateFinancialLogRequest request) {
        FinancialLogEntry.Snapshot snapshot = FinancialLogApiMapper.mapRequestToSnapshot(request);
        FinancialLogEntry logEntry = financialLogDomainService.create(snapshot, FinancialLogApi.Source.MANUAL);

        FinancialLogEntry saved = financialLogRepository.save(logEntry);

        return FinancialLogApiMapper.mapToFinancialLogListItemResponse(saved.toSnapshot());
    }

    public FinancialLogApi.FinancialLogResponse getDetailedByUuid(@NonNull UUID uuid) {
        Pair<FinancialLogEntry, List<FinancialLogHistory>> byUuidWithHistory =
                financialLogRepository.findByUuidWithHistory(uuid);
        return FinancialLogApiMapper.mapToFinancialLogResponseWithHistory(
                byUuidWithHistory.getFirst().toSnapshot(), byUuidWithHistory.getSecond());
    }

    @Transactional
    public FinancialLogApi.FinancialLogListItemResponse update(
            @NonNull UUID uuid, @NonNull FinancialLogApi.UpdateFinancialLogRequest request, @NonNull String userEmail) {
        FinancialLogEntry.Snapshot snapshot = FinancialLogApiMapper.mapRequestToSnapshot(request);

        FinancialLogEntry oldEntry = financialLogRepository.findByUuid(uuid);
        FinancialLogEntry.Snapshot oldEntrySnapshot = oldEntry.toSnapshot();

        FinancialLogEntry saved = financialLogRepository.save(
                financialLogDomainService.update(snapshot, oldEntry, request.modificationCause()));

        messagePublisher.publish(new FinancialLogApi.CreateFinancialLogHistory(
                buildHistorySnapshots(List.of(oldEntrySnapshot), userEmail, request.modificationCause())));

        return FinancialLogApiMapper.mapToFinancialLogListItemResponse(saved.toSnapshot());
    }

    @Transactional
    public void createSystemLogs(@NonNull FinancialLogApi.CreateMultipleFinancialLogs command) {
        List<FinancialLogEntry> logEntries = command.requests().stream()
                .map(FinancialLogApiMapper::mapRequestToSnapshot)
                .map(snapshot -> financialLogDomainService.create(snapshot, FinancialLogApi.Source.SYSTEM))
                .toList();
        financialLogRepository.saveAll(logEntries);
    }

    public FinancialLogApi.FinancialLogSummaryResponse getSummaryWithPageByCategory(@NonNull FinancialLogApi.GetFinancialLogFiltersRequest filter) {
        FinancialLogRepository.PageWithTotals financialLogTotals = financialLogRepository.findPageWithTotals(filter);
        return FinancialLogApiMapper.mapToSummary(financialLogTotals);
    }

    @Transactional
    public void acceptDrafts(@NonNull List<UUID> uuids, @NonNull String userEmail) {
        List<FinancialLogEntry> allByUuids = financialLogRepository.findAllByUuids(Set.copyOf(uuids));
        financialLogRepository.acceptDrafts(uuids);

        messagePublisher.publish(new FinancialLogApi.CreateFinancialLogHistory(buildHistorySnapshots(
                CollectionUtils.map(allByUuids, FinancialLogEntry::toSnapshot),
                userEmail,
                FinancialLogApi.Action.DRAFT_ACCEPTED.getValue())));
    }

    @Transactional
    public void delete(@NonNull List<UUID> uuids) {
        financialLogRepository.delete(uuids);
    }

    public List<FinancialLogApi.FinancialLogHistoryResponse> getHistoryByFinancialLogUuids(@NonNull UUID uuids) {
        List<FinancialLogHistory> history = financialLogRepository.findHistoriesByFinancialLogUuids(uuids);
        return FinancialLogApiMapper.mapToHistoryResponse(history);
    }

    public List<FinancialLogApi.FinancialLogListItemResponse> findFinancialLogsByReferences(
            @NonNull Set<FinancialLogApi.Reference> references) {
        return financialLogRepository.findFinancialLogsByReferences(references).stream()
                .map(FinancialLogEntry::toSnapshot)
                .map(FinancialLogApiMapper::mapToFinancialLogListItemResponse)
                .toList();
    }

    public FinancialLogApi.DailySummaryResponse getDailySummary(FinancialLogApi.GetFinancialLogFiltersRequest request) {
        return financialLogRepository.getDailySummary(request);
    }

    private Map<UUID, FinancialLogHistory.Snapshot> buildHistorySnapshots(
            List<FinancialLogEntry.Snapshot> snapshots, String user, String action) {
        return snapshots.stream()
                .collect(Collectors.toMap(
                        FinancialLogEntry.Snapshot::uuid,
                        snapshot -> FinancialLogApiMapper.buildHistorySnapshot(snapshot, user, action)));
    }
}
