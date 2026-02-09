package codeyourbrand.javainterview.common.currencyconverter;
import codeyourbrand.javainterview.common.model.CurrencyCode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("local")
public class FakeCurrencyConverterService extends CurrencyConverterService {
    private static final Map<Pair<CurrencyCode, CurrencyCode>, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    public FakeCurrencyConverterService() {
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.PLN, CurrencyCode.EUR), BigDecimal.valueOf(0.2222));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.PLN, CurrencyCode.USD), BigDecimal.valueOf(0.2677));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.PLN, CurrencyCode.GBP), BigDecimal.valueOf(0.2117));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.PLN, CurrencyCode.AED), BigDecimal.valueOf(0.9830));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.PLN, CurrencyCode.RON), BigDecimal.valueOf(1.0860));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.EUR, CurrencyCode.USD), BigDecimal.valueOf(1.2043));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.EUR, CurrencyCode.GBP), BigDecimal.valueOf(0.9520));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.EUR, CurrencyCode.AED), BigDecimal.valueOf(4.4221));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.EUR, CurrencyCode.RON), BigDecimal.valueOf(4.8720));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.USD, CurrencyCode.GBP), BigDecimal.valueOf(0.7910));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.USD, CurrencyCode.AED), BigDecimal.valueOf(3.6725));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.USD, CurrencyCode.RON), BigDecimal.valueOf(4.0400));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.GBP, CurrencyCode.AED), BigDecimal.valueOf(4.6440));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.GBP, CurrencyCode.RON), BigDecimal.valueOf(5.1100));
        EXCHANGE_RATES.put(new ImmutablePair<>(CurrencyCode.AED, CurrencyCode.RON), BigDecimal.valueOf(1.1000));

        Map<Pair<CurrencyCode, CurrencyCode>, BigDecimal> reverseRates = new HashMap<>();
        for (Map.Entry<Pair<CurrencyCode, CurrencyCode>, BigDecimal> entry : EXCHANGE_RATES.entrySet()) {
            Pair<CurrencyCode, CurrencyCode> key = entry.getKey();
            CurrencyCode fromCurrency = key.getLeft();
            CurrencyCode toCurrency = key.getRight();
            BigDecimal rate = entry.getValue();
            reverseRates.put(
                    new ImmutablePair<>(toCurrency, fromCurrency), BigDecimal.ONE.divide(rate, 5, RoundingMode.DOWN));
        }
        EXCHANGE_RATES.putAll(reverseRates);
    }

    @Override
    protected Map<Pair<CurrencyCode, CurrencyCode>, BigDecimal> getExchangeRates() {
        return EXCHANGE_RATES;
    }
}
