package codeyourbrand.javainterview.common.utils;

import java.util.Collection;
import java.util.List;
import java.util.SequencedCollection;
import java.util.function.Function;

public final class CollectionUtils {
    private CollectionUtils() {
        // Prevent instantiation
    }

    public static <T, V> List<V> map(List<T> elements, Function<T, V> mapper) {
        if (elements == null) {
            return List.of();
        }
        return elements.stream().map(mapper).toList();
    }

    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    public static <T> boolean addIfNotNull(Collection<T> collection, T elem) {
        if (elem != null) {
            collection.add(elem);
            return true;
        }
        return false;
    }

    public static <T> T getFirstOrNull(SequencedCollection<T> collection) {
        return collection == null || collection.isEmpty() ? null : collection.getFirst();
    }
}
