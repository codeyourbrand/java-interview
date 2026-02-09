package codeyourbrand.javainterview.common.messaging;

import java.time.Instant;
import java.util.UUID;

public interface Message {
    Instant occurredAt();

    UUID messageId();
}
