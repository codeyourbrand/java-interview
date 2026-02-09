package codeyourbrand.javainterview.financiallog.infrastructure.persistence;

import codeyourbrand.javainterview.common.specification.PredicateBuilder;
import codeyourbrand.javainterview.financiallog.application.FinancialLogApi;
import codeyourbrand.javainterview.financiallog.domain.FinancialLogEntry;
import codeyourbrand.javainterview.financiallog.domain.Reference;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static codeyourbrand.javainterview.common.specification.PredicateBuilder.*;

@RequiredArgsConstructor
class FinancialLogEntrySpecification implements Specification<FinancialLogEntry> {
    private final FinancialLogApi.GetFinancialLogFiltersRequest filters;

    @Override
    public Predicate toPredicate(
            @NonNull Root<FinancialLogEntry> root,
            @NonNull CriteriaQuery<?> query,
            @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        var criteriaData = PredicateBuilder.build(criteriaBuilder, root);

        predicates.add(conjunction(filters.getStatus(), FinancialLogEntry.STATUS, criteriaData));
        predicates.add(conjunction(filters.getCategory(), FinancialLogEntry.CATEGORY, criteriaData));
        predicates.add(likeIgnoreCase(filters.getName(), FinancialLogEntry.NAME, criteriaData));
        predicates.add(greaterThanOrEqual(filters.getSettleDateFrom(), FinancialLogEntry.SETTLE_DATE, criteriaData));
        predicates.add(lessThanOrEqual(filters.getSettleDateTo(), FinancialLogEntry.SETTLE_DATE, criteriaData));
        predicates.add(greaterThanOrEqual(filters.getValueFrom(), FinancialLogEntry.AED_AMOUNT, criteriaData));
        predicates.add(lessThanOrEqual(filters.getValueTo(), FinancialLogEntry.AED_AMOUNT, criteriaData));
        predicates.add(oneOfJsonb(filters.getTags(), FinancialLogEntry.TAGS, criteriaData));
        predicates.add(greaterThanOrEqual(filters.getCreatedAtFrom(), FinancialLogEntry.CREATED_AT, criteriaData));
        predicates.add(lessThanOrEqual(filters.getCreatedAtTo(), FinancialLogEntry.CREATED_AT, criteriaData));
        predicates.add(lessThan(filters.getCreatedAtBefore(), FinancialLogEntry.CREATED_AT, criteriaData));
        predicates.add(conjunction(filters.getSource(), FinancialLogEntry.SOURCE, criteriaData));
        predicates.add(conjunctionPath(
                filters.getReferenceBusinessId(), criteriaData, FinancialLogEntry.REFERENCE, Reference.BUSINESS_ID));

        return predicates.stream().filter(Objects::nonNull).reduce(criteriaBuilder.conjunction(), criteriaBuilder::and);
    }
}
