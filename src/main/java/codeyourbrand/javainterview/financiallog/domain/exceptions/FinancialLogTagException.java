package codeyourbrand.javainterview.financiallog.domain.exceptions;

import codeyourbrand.javainterview.common.exception.AbstractRuntimeException;
import codeyourbrand.javainterview.common.model.ApplicationErrorCode;

public class FinancialLogTagException extends AbstractRuntimeException {
    private FinancialLogTagException(String message, ApplicationErrorCode errorCode) {
        super(message, errorCode);
    }

    public static FinancialLogTagException tagNameNotUniqueInCategory(String tag) {
        return new FinancialLogTagException(
                "Financial log tag already exists: " + tag, ApplicationErrorCode.DATA_VALIDATION_ERROR);
    }

    public static FinancialLogTagException tagNameNotGiven() {
        return new FinancialLogTagException(
                "Financial log tag name not given", ApplicationErrorCode.DATA_VALIDATION_ERROR);
    }

    public static FinancialLogTagException tagCategoryNotGiven() {
        return new FinancialLogTagException(
                "Financial log tag category not given", ApplicationErrorCode.DATA_VALIDATION_ERROR);
    }
}
