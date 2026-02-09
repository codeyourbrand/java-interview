package codeyourbrand.javainterview.common.model;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Data
public class PaginationRequest {
    protected int page = 0;
    protected int size = 10;

    public PageRequest getPageRequest() {
        return PageRequest.of(getPage(), getSize());
    }

    public PageRequest getPageRequest(Sort sort) {
        if (!hasIdSort(sort)) {
            sort = sort.and(Sort.by("id"));
        }

        return PageRequest.of(getPage(), getSize(), sort);
    }

    private boolean hasIdSort(Sort sort) {
        return sort.stream().anyMatch(order -> order.getProperty().equals("id"));
    }
}
