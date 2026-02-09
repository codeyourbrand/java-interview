package codeyourbrand.javainterview.financiallog.application;

import codeyourbrand.javainterview.common.annotations.MessageListener;
import codeyourbrand.javainterview.financiallog.infrastructure.persistence.FinancialLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
class CreateFinancialLogHistoryCommandHandler {
    private final FinancialLogRepository financialLogRepository;

    @MessageListener
    void onCreateFinancialLogHistory(FinancialLogApi.CreateFinancialLogHistory command) {
        if (command.entryUuidToOldSnapshot() != null) {
            financialLogRepository.createHistory(command.entryUuidToOldSnapshot());
        }
    }
}
