package codeyourbrand.javainterview.financiallog.application;


import codeyourbrand.javainterview.common.model.BasePaginatedResponse;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogEntry;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogHistory;
import codeyourbrand.javainterview.financiallog.infrastructure.persistence.FinancialLogRepository;

import java.util.List;
import java.util.stream.Collectors;

final class FinancialLogApiMapper {
    private FinancialLogApiMapper() {
        // prevent instantiation
    }

    static FinancialLogEntry.Snapshot mapRequestToSnapshot(FinancialLogApi.CreateFinancialLogRequest request) {
        return FinancialLogEntry.Snapshot.builder()
                .status(request.status())
                .name(request.name())
                .money(request.money())
                .settleDate(request.settleDate())
                .category(request.category())
                .reference(request.reference())
                .notes(request.notes())
                .tags(request.tags())
                .reference(request.reference())
                .tags(request.tags())
                .build();
    }

    static FinancialLogEntry.Snapshot mapRequestToSnapshot(FinancialLogApi.UpdateFinancialLogRequest request) {
        return FinancialLogEntry.Snapshot.builder()
                .category(request.category())
                .name(request.name())
                .settleDate(request.settleDate())
                .money(request.money())
                .tags(request.tags())
                .notes(request.notes())
                .reference(request.reference())
                .build();
    }

    static FinancialLogApi.FinancialLogResponse mapToFinancialLogResponseWithHistory(
            FinancialLogEntry.Snapshot snapshot, List<FinancialLogHistory> history) {
        return new FinancialLogApi.FinancialLogResponse(
                snapshot.uuid(),
                snapshot.sequenceNumber(),
                snapshot.name(),
                snapshot.status(),
                snapshot.category(),
                snapshot.settleDate(),
                snapshot.money(),
                snapshot.aedAmount(),
                snapshot.reference(),
                snapshot.tags(),
                snapshot.notes(),
                snapshot.source(),
                snapshot.createdAt(),
                mapToHistoryResponse(history));
    }

    static List<FinancialLogApi.FinancialLogHistoryResponse> mapToHistoryResponse(List<FinancialLogHistory> snapshots) {
        return snapshots.stream()
                .map(FinancialLogHistory::toSnapshot)
                .map(snapshot -> new FinancialLogApi.FinancialLogHistoryResponse(
                        snapshot.createdBy(),
                        snapshot.financialLogName(),
                        snapshot.settleDate(),
                        snapshot.originalMoney(),
                        snapshot.aedAmount(),
                        snapshot.action(),
                        snapshot.createdAt()))
                .toList();
    }

    static FinancialLogApi.FinancialLogSummaryResponse mapToSummary(
            FinancialLogRepository.PageWithTotals financialLogTotals) {
        FinancialLogApi.FinancialLogSummaryResponse summary = new FinancialLogApi.FinancialLogSummaryResponse(
                financialLogTotals.income(),
                financialLogTotals.cost(),
                financialLogTotals.profit(),
                FinancialLogApiMapper.mapToListItems(financialLogTotals.page().getContent()));
        summary.setPagination(new BasePaginatedResponse.MetadataResponse(
                financialLogTotals.page().getNumber(),
                financialLogTotals.page().getSize(),
                (int) financialLogTotals.page().getTotalElements(),
                financialLogTotals.page().getTotalPages()));
        return summary;
    }

    private static List<FinancialLogApi.FinancialLogListItemResponse> mapToListItems(List<FinancialLogEntry> entries) {
        return entries.stream()
                .map(FinancialLogEntry::toSnapshot)
                .map(FinancialLogApiMapper::mapToFinancialLogListItemResponse)
                .toList();
    }

    static FinancialLogApi.FinancialLogListItemResponse mapToFinancialLogListItemResponse(
            FinancialLogEntry.Snapshot snapshot) {
        return new FinancialLogApi.FinancialLogListItemResponse(
                snapshot.uuid(),
                snapshot.sequenceNumber(),
                snapshot.name(),
                snapshot.settleDate(),
                snapshot.aedAmount(),
                snapshot.category(),
                snapshot.notes(),
                snapshot.tags(),
                snapshot.reference(),
                snapshot.createdAt(),
                snapshot.source());
    }

    static FinancialLogHistory.Snapshot buildHistorySnapshot(
            FinancialLogEntry.Snapshot snapshot, String createdBy, String action) {
        return FinancialLogHistory.Snapshot.builder()
                .createdBy(createdBy)
                .financialLogName(snapshot.name())
                .settleDate(snapshot.settleDate())
                .originalMoney(snapshot.money())
                .aedAmount(snapshot.aedAmount())
                .action(action)
                .status(snapshot.status())
                .tags(snapshot.tags())
                .notes(snapshot.notes())
                .category(snapshot.category())
                .reference(snapshot.reference())
                .build();
    }
}
