package codeyourbrand.javainterview.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor
@Getter
@Builder
@Embeddable
@ToString
@EqualsAndHashCode
public class Money {
    @Column(name = "amount")
    @NotNull
    private BigDecimal amount;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    @NotNull
    private CurrencyCode currency;

    public Money(BigDecimal amount, CurrencyCode currency) {
        this.amount = requireNonNull(amount, "amount").setScale(2, RoundingMode.DOWN);
        this.currency = requireNonNull(currency, "currency");
    }

    public static Money of(String amount, CurrencyCode currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money zero(CurrencyCode currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }

    public Money add(Money other, int scale, RoundingMode roundingMode) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(amount.add(other.amount).setScale(scale, roundingMode), currency);
    }

    public Money subtract(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money subtract(Money other, int scale, RoundingMode roundingMode) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(amount.subtract(other.amount).setScale(scale, roundingMode), currency);
    }

    public Money multiply(double multiplier) {
        return new Money(amount.multiply(BigDecimal.valueOf(multiplier)), currency);
    }

    public Money multiply(BigDecimal multiplier, int scale, RoundingMode roundingMode) {
        return new Money(amount.multiply(multiplier).setScale(scale, roundingMode), currency);
    }

    public Money divide(BigDecimal divider) {
        return new Money(amount.divide(divider, RoundingMode.DOWN), currency);
    }

    public Money multiply(BigDecimal multiplier) {
        return new Money(amount.multiply(multiplier), currency);
    }

    public Money negate() {
        return new Money(amount.negate(), currency);
    }

    public int compareTo(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money with different currencies");
        }
        return amount.compareTo(other.amount);
    }

    @PrePersist
    @PreUpdate
    private void ensureScaleBeforeSave() {
        if (amount != null) {
            amount = amount.setScale(2, RoundingMode.DOWN);
        }
    }

    @PostLoad
    private void ensureScaleAfterLoad() {
        if (amount != null) {
            amount = amount.setScale(2, RoundingMode.DOWN);
        }
    }
}
