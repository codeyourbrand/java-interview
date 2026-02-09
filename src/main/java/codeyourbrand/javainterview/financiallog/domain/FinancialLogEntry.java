package codeyourbrand.javainterview.financiallog.domain;


import codeyourbrand.javainterview.common.model.Money;
import codeyourbrand.javainterview.financiallog.application.FinancialLogApi;
import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "financial_log_entry", schema = "financial_log")
@Getter(AccessLevel.PACKAGE)
public class FinancialLogEntry {
    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    @EqualsAndHashCode.Include
    private UUID uuid;

    @Column(name = "sequence_number", insertable = false, updatable = false)
    private Long sequenceNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FinancialLogApi.Status status;

    @NotNull
    @Column(name = "name")
    private String name;

    @NotNull
    private Money money;

    @NotNull
    @Column(name = "aed_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal aedAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private FinancialLogApi.Category category;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private FinancialLogApi.Source source;

    @NotNull
    @Column(name = "settle_date")
    private LocalDate settleDate;

    @Column(name = "notes")
    private String notes;

    private Reference reference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb", nullable = false)
    private final Set<String> tags = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    FinancialLogEntry(Snapshot snapshot, FinancialLogApi.Source source, BigDecimal aedAmount) {
        this.status = snapshot.status();
        this.name = snapshot.name();
        this.money = snapshot.money();
        this.aedAmount = aedAmount;
        this.category = snapshot.category();
        this.source = source;
        this.settleDate = snapshot.settleDate();
        this.notes = snapshot.notes();
        this.reference = mapToDomainReference(snapshot.reference());
        this.tags.addAll(Optional.ofNullable(snapshot.tags()).orElse(new HashSet<>()));
    }

    FinancialLogEntry update(Snapshot snapshot, BigDecimal aedAmount) {
        return switch (status) {
            case ACCEPTED -> updateCommonProperties(snapshot, aedAmount).updateTags(snapshot, updateTagsInAccepted());
            case DRAFT -> updateCommonProperties(snapshot, aedAmount)
                    .updateTags(snapshot, updateTagsInDraft())
                    .replaceReference(snapshot);
        };
    }

    private FinancialLogEntry updateCommonProperties(Snapshot snapshot, BigDecimal aedAmount) {
        if (snapshot.category() != null) {
            this.category = snapshot.category();
        }
        if (snapshot.name() != null) {
            this.name = snapshot.name();
        }
        if (snapshot.money() != null && aedAmount != null) {
            this.money = snapshot.money();
            this.aedAmount = aedAmount;
        }
        if (snapshot.settleDate() != null) {
            this.settleDate = snapshot.settleDate();
        }
        if (snapshot.notes() != null) {
            this.notes = snapshot.notes();
        }
        return this;
    }

    private FinancialLogEntry updateTags(Snapshot snapshot, Consumer<Set<String>> updateTagsFunc) {
        if (snapshot.tags() != null) {
            updateTagsFunc.accept(snapshot.tags());
        }
        return this;
    }

    private FinancialLogEntry replaceReference(Snapshot snapshot) {
        this.reference = mapToDomainReference(snapshot.reference());
        return this;
    }

    private Consumer<Set<String>> updateTagsInDraft() {
        return tags -> {
            this.tags.clear();
            this.tags.addAll(tags);
        };
    }

    private Consumer<Set<String>> updateTagsInAccepted() {
        return this.tags::addAll;
    }

    static Reference mapToDomainReference(FinancialLogApi.Reference reference) {
        return reference == null ? null : new Reference(reference.id(), reference.type(), reference.businessId());
    }

    protected FinancialLogEntry() {}

    public Snapshot toSnapshot() {
        return Snapshot.builder()
                .uuid(uuid)
                .sequenceNumber(sequenceNumber)
                .status(status)
                .name(name)
                .category(category)
                .settleDate(settleDate)
                .money(money)
                .aedAmount(aedAmount)
                .reference(mapToApiReference(reference))
                .notes(notes)
                .source(source)
                .tags(new HashSet<>(tags))
                .createdAt(createdAt)
                .build();
    }

    public UUID toUuidOnly() {
        return uuid;
    }

    static FinancialLogApi.Reference mapToApiReference(Reference reference) {
        return reference == null
                ? null
                : new FinancialLogApi.Reference(reference.getId(), reference.getType(), reference.getBusinessId());
    }

    @Builder
    public record Snapshot(
            UUID uuid,
            Long sequenceNumber,
            FinancialLogApi.Status status,
            String name,
            FinancialLogApi.Category category,
            LocalDate settleDate,
            Money money,
            BigDecimal aedAmount,
            FinancialLogApi.Reference reference,
            String notes,
            FinancialLogApi.Source source,
            Set<String> tags,
            LocalDateTime createdAt) {}

    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String NOTES = "notes";
    public static final String SETTLE_DATE = "settleDate";
    public static final String AED_AMOUNT = "aedAmount";
    public static final String SOURCE = "source";
    public static final String VERSION = "version";
    public static final String UUID = "uuid";
    public static final String TAGS = "tags";
    public static final String REFERENCE = "reference";
    public static final String CREATED_AT = "createdAt";
    public static final String MONEY = "money";
    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String STATUS = "status";
}
