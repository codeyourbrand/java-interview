package codeyourbrand.javainterview.common.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

public class PredicateBuilder {
    private PredicateBuilder() {}

    @Getter
    @AllArgsConstructor
    public static class CriteriaData<T> {
        private Root<T> root;
        private CriteriaBuilder criteriaBuilder;
    }

    public static <T> CriteriaData<T> build(CriteriaBuilder criteriaBuilder, Root<T> root) {
        return new CriteriaData<>(root, criteriaBuilder);
    }

    public static <T> Predicate isNotNull(String columnValue, CriteriaData<T> criteria) {
        return criteria.getCriteriaBuilder().isNotNull(criteria.getRoot().get(columnValue));
    }

    public static <T> Predicate conjunction(Object value, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder().equal(criteria.getRoot().get(columnValue), value)
                : null;
    }

    public static <T, Y> Predicate conjunction(
            Object value, Join<T, Y> joinValue, String columnValue, CriteriaData<T> criteria) {
        return value != null ? criteria.getCriteriaBuilder().equal(joinValue.get(columnValue), value) : null;
    }

    public static <T, Y extends Comparable<? super Y>> Predicate greaterThanOrEqual(
            Y value, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder()
                        .greaterThanOrEqualTo(criteria.getRoot().get(columnValue), value)
                : null;
    }

    public static <T, E> Predicate greaterThanOrEqualNumber(
            Number value, Join<T, E> joinValue, String columnValue, CriteriaData<T> criteria) {
        return value != null ? criteria.getCriteriaBuilder().ge(joinValue.get(columnValue), value) : null;
    }

    public static <T, E> Predicate lessThanOrEqualNumber(
            Number value, Join<T, E> joinValue, String columnValue, CriteriaData<T> criteria) {
        return value != null ? criteria.getCriteriaBuilder().le(joinValue.get(columnValue), value) : null;
    }

    public static <T, Y extends Comparable<? super Y>> Predicate greaterThanOrEqual(
            Y value, Join<T, Y> joinValue, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder().greaterThanOrEqualTo(joinValue.get(columnValue), value)
                : null;
    }

    public static <T> Predicate conjunctionPath(Object value, CriteriaData<T> criteria, String... path) {
        if (value == null || path == null || path.length == 0) {
            return null;
        }

        var p = criteria.getRoot().get(path[0]);
        for (int i = 1; i < path.length; i++) {
            p = p.get(path[i]);
        }
        return criteria.getCriteriaBuilder().equal(p, value);
    }

    public static <T, Y extends Comparable<? super Y>> Predicate lessThanOrEqual(
            Y value, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder()
                        .lessThanOrEqualTo(criteria.getRoot().get(columnValue), value)
                : null;
    }

    public static <T, Y extends Comparable<? super Y>> Predicate lessThan(
            Y value, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder().lessThan(criteria.getRoot().get(columnValue), value)
                : null;
    }

    public static <T, Y extends Comparable<? super Y>> Predicate lessThanOrEqual(
            Y value, Join<T, Y> joinValue, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder().lessThanOrEqualTo(joinValue.get(columnValue), value)
                : null;
    }

    public static <T> Predicate likeIgnoreCase(String value, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder()
                        .like(
                                criteria.getCriteriaBuilder()
                                        .lower(criteria.getRoot().get(columnValue)),
                                "%" + value.toLowerCase() + "%")
                : null;
    }

    public static <T, Y> Predicate likeIgnoreCase(
            String value, Join<T, Y> joinValue, String columnValue, CriteriaData<T> criteria) {
        return value != null
                ? criteria.getCriteriaBuilder()
                        .like(
                                criteria.getCriteriaBuilder().lower(joinValue.get(columnValue)),
                                "%" + value.toLowerCase() + "%")
                : null;
    }

    public static <T> Predicate isNull(String columnValue, CriteriaData<T> criteria) {
        return criteria.getCriteriaBuilder().isNull(criteria.getRoot().get(columnValue));
    }

    public static <T, V> Predicate oneOfJsonb(Collection<V> values, String columnValue, CriteriaData<T> criteria) {
        CriteriaBuilder cb = criteria.getCriteriaBuilder();
        if (values == null || values.isEmpty()) {
            return cb.conjunction();
        }

        Predicate p = cb.disjunction();
        for (V v : values) {
            p = cb.or(
                    p,
                    cb.isTrue(cb.function(
                            "jsonb_exists", Boolean.class, criteria.getRoot().get(columnValue), cb.literal(v))));
        }
        return p;
    }
}
