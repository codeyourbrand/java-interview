package codeyourbrand.javainterview.common.currencyconverter;

import codeyourbrand.javainterview.common.model.CurrencyCode;
import codeyourbrand.javainterview.common.model.Money;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
public abstract class CurrencyConverterService {
    public BigDecimal convert(CurrencyCode sourceCurrency, CurrencyCode targetCurrency, BigDecimal amount) {
        if (amount == null || sourceCurrency == targetCurrency) {
            return amount;
        }

        Pair<CurrencyCode, CurrencyCode> exchangePair = ImmutablePair.of(sourceCurrency, targetCurrency);

        BigDecimal exchangeRate = getExchangeRates().get(exchangePair);
        return amount.multiply(exchangeRate).setScale(2, RoundingMode.DOWN);
    }

    public Money convert(Money money, CurrencyCode targetCurrency) {
        BigDecimal convertedAmount = convert(money.getCurrency(), targetCurrency, money.getAmount());
        return new Money(convertedAmount, targetCurrency);
    }

    protected abstract Map<Pair<CurrencyCode, CurrencyCode>, BigDecimal> getExchangeRates();
}
