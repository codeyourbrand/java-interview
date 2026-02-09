package codeyourbrand.javainterview.common.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public abstract class BasePaginatedResponse {
    @NotNull
    private MetadataResponse pagination;

    @Data
    @AllArgsConstructor
    public static class MetadataResponse {
        @NotNull
        private int number;

        @NotNull
        private int size;

        @NotNull
        private int totalElements;

        @NotNull
        private int totalPages;
    }
}
