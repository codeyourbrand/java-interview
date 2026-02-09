package codeyourbrand.javainterview.common.messaging;

import java.util.List;

public interface MessagePublisher {

    void publish(Message message);

    void publish(List<? extends Message> messages);
}
