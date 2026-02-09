package codeyourbrand.javainterview.financiallog.domain.exceptions;

import codeyourbrand.javainterview.common.exception.AbstractRuntimeException;
import codeyourbrand.javainterview.common.model.ApplicationErrorCode;

public class FinancialLogException extends AbstractRuntimeException {
    private FinancialLogException(String message, ApplicationErrorCode errorCode) {
        super(message, errorCode);
    }

    public static FinancialLogException missingModificationReason() {
        return new FinancialLogException(
                "Modification reason must be provided when updating the ACCEPTED financial log entry.",
                ApplicationErrorCode.DATA_VALIDATION_ERROR);
    }
}
