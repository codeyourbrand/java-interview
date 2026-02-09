package codeyourbrand.javainterview.financiallog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter(AccessLevel.PACKAGE)
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class Reference {
    public static final String BUSINESS_ID = "reference_business_id";
    public static final String TYPE = "reference_type";
    public static final String ID = "reference_id";
    @Column(name = ID)
    private String id;

    @Column(name = TYPE)
    private String type;

    @Column(name = BUSINESS_ID)
    private String businessId;
}
