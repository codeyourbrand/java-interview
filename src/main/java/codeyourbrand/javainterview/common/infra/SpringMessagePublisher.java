package codeyourbrand.javainterview.common.infra;

import codeyourbrand.javainterview.common.messaging.Message;
import codeyourbrand.javainterview.common.messaging.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional
class SpringMessagePublisher implements MessagePublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void publish(Message message) {
        eventPublisher.publishEvent(message);
    }

    @Override
    public void publish(List<? extends Message> messages) {
        messages.forEach(this::publish);
    }
}
