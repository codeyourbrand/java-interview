package codeyourbrand.javainterview.financiallog.infrastructure.persistence;

import codeyourbrand.javainterview.common.specification.PredicateBuilder;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogEntry;
import codeyourbrand.javainterview.financiallog.domain.Reference;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Set;

@Repository
@RequiredArgsConstructor
class CalculateFinancialLogTotals {
    private final EntityManager entityManager;

    FinancialSummary calculateFinancialSummary(Specification<FinancialLogEntry> spec) {
        var incomeCostAndProfit = calculateIncomeCostAndProfit(spec);
        var dailyOrders = countOrders(spec);
        return new FinancialSummary(incomeCostAndProfit.income(), incomeCostAndProfit.profit(), dailyOrders);
    }

    private static final Set<String> ORDER_REFERENCE_TYPES = Set.of("ATTRACTION", "PACKAGE_HOLIDAY", "STAY");

    private Long countOrders(Specification<FinancialLogEntry> spec) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(FinancialLogEntry.class);

        var predicate = spec == null ? cb.conjunction() : spec.toPredicate(root, cq, cb);
        var criteriaData = PredicateBuilder.build(cb, root);
        var referenceNotNull = PredicateBuilder.isNotNull(FinancialLogEntry.REFERENCE, criteriaData);
        var referenceTypeIsOrder =
                root.get(FinancialLogEntry.REFERENCE).get(Reference.TYPE).in(ORDER_REFERENCE_TYPES);

        cq.select(cb.countDistinct(root.get(FinancialLogEntry.REFERENCE).get(Reference.ID)))
                .where(cb.and(predicate, referenceNotNull, referenceTypeIsOrder));

        return entityManager.createQuery(cq).getSingleResult();
    }

    IncomeCostAndProfit calculateIncomeCostAndProfit(Specification<FinancialLogEntry> spec) {
        var cb = entityManager.getCriteriaBuilder();
        var cq = cb.createQuery(IncomeCostAndProfit.class);
        var root = cq.from(FinancialLogEntry.class);

        Expression<BigDecimal> incomeCase = cb.<BigDecimal>selectCase()
                .when(
                        cb.greaterThanOrEqualTo(root.get(FinancialLogEntry.AED_AMOUNT), BigDecimal.ZERO),
                        root.get(FinancialLogEntry.AED_AMOUNT))
                .otherwise(BigDecimal.ZERO);

        Expression<BigDecimal> costCase = cb.<BigDecimal>selectCase()
                .when(
                        cb.lessThan(root.get(FinancialLogEntry.AED_AMOUNT), BigDecimal.ZERO),
                        root.get(FinancialLogEntry.AED_AMOUNT))
                .otherwise(BigDecimal.ZERO);

        var predicate = spec == null ? cb.conjunction() : spec.toPredicate(root, cq, cb);

        cq.select(cb.construct(
                        IncomeCostAndProfit.class,
                        cb.coalesce(cb.sum(incomeCase), BigDecimal.ZERO),
                        cb.coalesce(cb.sum(costCase), BigDecimal.ZERO)))
                .where(predicate);

        return entityManager.createQuery(cq).getSingleResult();
    }

    record IncomeCostAndProfit(BigDecimal income, BigDecimal cost) {
        public BigDecimal profit() {
            return income.add(cost);
        }
    }

    public record FinancialSummary(BigDecimal revenue, BigDecimal profit, Long orders) {}
}
