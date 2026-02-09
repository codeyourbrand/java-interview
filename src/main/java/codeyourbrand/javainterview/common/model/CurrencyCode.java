package codeyourbrand.javainterview.common.model;

import java.util.Arrays;

public enum CurrencyCode {
    PLN,
    USD,
    AED,
    EUR,
    GBP,
    RON;

    public static CurrencyCode fromString(String currencyCode) {
        return Arrays.stream(CurrencyCode.values())
                .filter(value -> value.name().equalsIgnoreCase(currencyCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid currency " + currencyCode));
    }
}
