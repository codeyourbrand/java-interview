package codeyourbrand.javainterview.financiallog.domain;

import codeyourbrand.javainterview.common.annotations.DomainService;
import codeyourbrand.javainterview.common.currencyconverter.CurrencyConverterService;
import codeyourbrand.javainterview.common.model.CurrencyCode;
import codeyourbrand.javainterview.common.model.Money;
import codeyourbrand.javainterview.financiallog.application.FinancialLogApi;
import codeyourbrand.javainterview.financiallog.domain.exceptions.FinancialLogException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@DomainService
@RequiredArgsConstructor
public class FinancialLogDomainService {
    private final CurrencyConverterService converterService;

    public FinancialLogEntry create(FinancialLogEntry.Snapshot snapshot, FinancialLogApi.Source source) {
        Money aedMoney = convertToAED(snapshot);
        return new FinancialLogEntry(snapshot, source, aedMoney.getAmount());
    }

    public FinancialLogEntry update(
            FinancialLogEntry.Snapshot snapshot, FinancialLogEntry entry, String modificationCause) {
        ensureModificationReasonIsGiven(snapshot.status(), modificationCause);
        return entry.update(snapshot, getConvertedAedAmount(snapshot));
    }

    private void ensureModificationReasonIsGiven(FinancialLogApi.Status status, String modificationCause) {
        if (status == FinancialLogApi.Status.ACCEPTED && (modificationCause == null || modificationCause.isBlank())) {
            throw FinancialLogException.missingModificationReason();
        }
    }

    private BigDecimal getConvertedAedAmount(FinancialLogEntry.Snapshot snapshot) {
        return snapshot.money() != null ? convertToAED(snapshot).getAmount() : null;
    }

    private Money convertToAED(FinancialLogEntry.Snapshot snapshot) {
        return converterService.convert(snapshot.money(), CurrencyCode.AED);
    }
}
