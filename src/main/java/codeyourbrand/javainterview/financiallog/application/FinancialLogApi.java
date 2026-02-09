package codeyourbrand.javainterview.financiallog.application;

import codeyourbrand.javainterview.common.messaging.Message;
import codeyourbrand.javainterview.common.model.BasePaginatedResponse;
import codeyourbrand.javainterview.common.model.Money;
import codeyourbrand.javainterview.common.model.PaginationRequest;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogEntry;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogHistory;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface FinancialLogApi {
    enum Category {
        EMPLOYEES,
        HOLIDAY_HOMES,
        TOURS_AND_TRAVEL,
        OPERATIONS;

        public static Category of(String category) {
            return Category.valueOf(category);
        }
    }

    enum Source {
        MANUAL,
        SYSTEM
    }

    enum Status {
        DRAFT,
        ACCEPTED
    }

    record CreateMultipleFinancialLogs(List<CreateFinancialLogRequest> requests, Instant occurredAt, UUID messageId)
            implements Message {
        public CreateMultipleFinancialLogs(List<CreateFinancialLogRequest> entries) {
            this(entries, Instant.now(), UUID.randomUUID());
        }
    }

    record FinancialLogResponse(
            @NonNull UUID uuid,
            @NonNull Long sequenceNumber,
            @NonNull String name,
            @NonNull Status status,
            @NonNull Category category,
            @NonNull LocalDate settleDate,
            @NonNull Money originalValue,
            @NonNull BigDecimal aedValue,
            Reference reference,
            @NonNull Set<String> tags,
            String notes,
            @NonNull Source source,
            @NonNull LocalDateTime createdAt,
            @NonNull List<FinancialLogHistoryResponse> history) {}

    record FinancialLogHistoryResponse(
            @NonNull String user,
            @NonNull String financialLogName,
            @NonNull LocalDate settleDate,
            @NonNull Money originalValue,
            @NonNull BigDecimal aedValue,
            @NonNull String cause,
            LocalDateTime createdAt) {}

    record CreateFinancialLogRequest(
            @NonNull Category category,
            @NonNull String name,
            @NonNull LocalDate settleDate,
            @NonNull Money money,
            @NonNull Status status,
            Reference reference,
            Set<String> tags,
            String notes) {

        public CreateFinancialLogRequest(
                @NonNull Category category,
                @NonNull String name,
                @NonNull LocalDate settleDate,
                @NonNull Money money,
                Reference reference,
                Set<String> tags,
                String notes) {
            this(category, name, settleDate, money, Status.ACCEPTED, reference, tags, notes);
        }
    }

    record UpdateFinancialLogRequest(
            Category category,
            String name,
            LocalDate settleDate,
            Money money,
            Status status,
            Set<String> tags,
            String modificationCause,
            String notes,
            Reference reference) {
        public UpdateFinancialLogRequest {
            if (status == Status.ACCEPTED && (modificationCause == null || modificationCause.isBlank())) {
                throw new IllegalArgumentException("Modification cause is required for ACCEPTED status");
            }
        }

        public UpdateFinancialLogRequest(
                Category category,
                String name,
                LocalDate settleDate,
                Money money,
                Set<String> tags,
                String modificationCause,
                String notes) {
            this(category, name, settleDate, money, Status.ACCEPTED, tags, modificationCause, notes, null);
        }

        public UpdateFinancialLogRequest(
                Category category,
                String name,
                LocalDate settleDate,
                Money money,
                Set<String> tags,
                String modificationCause) {
            this(category, name, settleDate, money, Status.ACCEPTED, tags, modificationCause, null, null);
        }
    }

    record FinancialLogListItemResponse(
            @NonNull UUID uuid,
            @NonNull Long sequenceNumber,
            @NonNull String name,
            @NonNull LocalDate settleDate,
            @NonNull BigDecimal aedAmount,
            @NonNull Category category,
            String notes,
            @NonNull Set<String> tags,
            Reference reference,
            @NonNull LocalDateTime createdAt,
            @NonNull Source source) {}

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    class FinancialLogSummaryResponse extends BasePaginatedResponse {
        private BigDecimal income;
        private BigDecimal cost;
        private BigDecimal profit;
        private List<FinancialLogListItemResponse> financialLogListItemResponses;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    class GetFinancialLogFiltersRequest extends PaginationRequest {
        private String name;
        private Category category;
        private Status status = Status.ACCEPTED;
        private LocalDate settleDateFrom;
        private LocalDate settleDateTo;
        private BigDecimal valueFrom;
        private BigDecimal valueTo;
        private Set<String> tags;

        @Getter(AccessLevel.NONE)
        private LocalDate createdAtFrom;

        @Getter(AccessLevel.NONE)
        private LocalDate createdAtTo;

        @Getter(AccessLevel.NONE)
        private LocalDate createdAtBefore;

        private Source source;
        private String referenceBusinessId;
        private SortBy sortBy = SortBy.SETTLE_DATE;
        private Sort.Direction sortDirection = Sort.Direction.DESC;

        @Override
        public PageRequest getPageRequest(Sort sort) {
            return PageRequest.of(getPage(), getSize(), sort);
        }

        public LocalDateTime getCreatedAtFrom() {
            return createdAtFrom != null ? createdAtFrom.atStartOfDay() : null;
        }

        public LocalDateTime getCreatedAtTo() {
            return createdAtTo != null ? createdAtTo.atTime(LocalTime.MAX) : null;
        }

        public LocalDateTime getCreatedAtBefore() {
            return createdAtBefore != null ? createdAtBefore.atStartOfDay() : null;
        }
    }

    @Getter
    enum SortBy {
        ID(FinancialLogEntry.SEQUENCE_NUMBER),
        NAME(FinancialLogEntry.NAME),
        SETTLE_DATE(FinancialLogEntry.SETTLE_DATE),
        VALUE(FinancialLogEntry.AED_AMOUNT);

        private final String value;

        SortBy(String value) {
            this.value = value;
        }
    }

    record Reference(@NonNull String id, @NonNull String type, String businessId) {}

    record UpdateFinancialLogTagRequest(@NonNull Category category, @NonNull String name) {
        public UpdateFinancialLogTagRequest {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Tag name cannot be blank");
            }
        }
    }

    record CreateFinancialLogTagRequest(@NonNull Category category, @NonNull String name) {
        public CreateFinancialLogTagRequest {
            if (name.isBlank()) {
                throw new IllegalArgumentException("Tag name cannot be blank");
            }
        }
    }

    record FinancialLogTagResponse(@NonNull UUID uuid, @NonNull String name, @NonNull Category category) {}

    record FinancialLogTagIdAndNameResponse(@NonNull UUID uuid, @NonNull String name) {}

    record FinancialLogTagsByCategoryResponse(
            @NonNull Map<Category, List<FinancialLogTagIdAndNameResponse>> tagsByCategory) {}

    record DailySummaryResponse(@NonNull BigDecimal revenue, @NonNull BigDecimal profit, @NonNull Long orders) {}

    @Getter
    enum Action {
        CREATED("The new financial log entry was created"),
        DRAFT_ACCEPTED("The draft financial log entry was accepted");
        private final String value;

        Action(String value) {
            this.value = value;
        }
    }

    record CreateFinancialLogHistory(
            List<UUID> financialLogEntriesUuids,
            Map<UUID, FinancialLogHistory.Snapshot> entryUuidToOldSnapshot,
            Instant occurredAt,
            UUID messageId)
            implements Message {
        public CreateFinancialLogHistory(Map<UUID, FinancialLogHistory.Snapshot> entryUuidToOldSnapshot) {
            this(null, entryUuidToOldSnapshot, Instant.now(), UUID.randomUUID());
        }
    }
}
