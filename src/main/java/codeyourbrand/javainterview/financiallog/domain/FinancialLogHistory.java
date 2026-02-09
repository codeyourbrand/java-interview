package codeyourbrand.javainterview.financiallog.domain;

import codeyourbrand.javainterview.common.model.CurrencyCode;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "financial_log_history", schema = "financial_log")
@Getter(AccessLevel.PACKAGE)
public class FinancialLogHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid")
    @EqualsAndHashCode.Include
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "financial_log_uuid",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_financial_log_entry"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FinancialLogEntry financialLogEntry;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @NotNull
    @Column(name = "financial_log_name")
    private String financialLogName;

    @NotNull
    @Column(name = "settle_date")
    private LocalDate settleDate;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "original_amount", precision = 19, scale = 2, nullable = false)),
        @AttributeOverride(
                name = "currency",
                column = @Column(name = "original_currency", length = 3, nullable = false))
    })
    private Money originalMoney;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
                name = "amount",
                column = @Column(name = "converted_amount", precision = 19, scale = 2, nullable = false)),
        @AttributeOverride(
                name = "currency",
                column = @Column(name = "converted_currency", length = 3, nullable = false))
    })
    private Money convertedMoney;

    @NotNull
    @Column(name = "action")
    private String action;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FinancialLogApi.Status status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb", nullable = false)
    private Set<String> tags;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private FinancialLogApi.Category category;

    private Reference reference;

    protected FinancialLogHistory() {}

    FinancialLogHistory(FinancialLogEntry financialLogEntry, Snapshot oldEntry) {
        this.financialLogEntry = financialLogEntry;
        this.createdBy = oldEntry.createdBy();
        this.financialLogName = oldEntry.financialLogName();
        this.settleDate = oldEntry.settleDate();
        this.originalMoney = oldEntry.originalMoney();
        this.convertedMoney = new Money(oldEntry.aedAmount(), CurrencyCode.AED);
        this.action = oldEntry.action();
        this.createdAt = LocalDateTime.now();
        this.status = oldEntry.status();
        this.tags = oldEntry.tags();
        this.notes = oldEntry.notes();
        this.category = oldEntry.category();
        this.reference = FinancialLogEntry.mapToDomainReference(oldEntry.reference());
    }

    // Only to resolve unprocessed history creation events, to be removed later
    public FinancialLogHistory(FinancialLogEntry entry) {
        this.financialLogEntry = entry;
        this.createdBy = "SYSTEM";
        this.financialLogName = entry.getName();
        this.settleDate = entry.getSettleDate();
        this.originalMoney = entry.getMoney();
        this.convertedMoney = new Money(entry.getAedAmount(), CurrencyCode.AED);
        this.action = FinancialLogApi.Action.CREATED.getValue();
        this.createdAt = LocalDateTime.now();
        this.status = entry.getStatus();
        this.tags = entry.getTags();
        this.notes = entry.getNotes();
    }

    public static FinancialLogHistory create(
            FinancialLogEntry financialLogEntry, Snapshot snapshot) {
        return new FinancialLogHistory(financialLogEntry, snapshot);
    }

    public Snapshot toSnapshot() {
        return new Snapshot(
                createdBy,
                financialLogName,
                settleDate,
                originalMoney,
                convertedMoney.getAmount(),
                action,
                createdAt,
                status,
                tags,
                notes,
                category,
                FinancialLogEntry.mapToApiReference(reference));
    }

    @Builder
    public record Snapshot(
            String createdBy,
            String financialLogName,
            LocalDate settleDate,
            Money originalMoney,
            BigDecimal aedAmount,
            String action,
            LocalDateTime createdAt,
            FinancialLogApi.Status status,
            Set<String> tags,
            String notes,
            FinancialLogApi.Category category,
            FinancialLogApi.Reference reference) {}
}
