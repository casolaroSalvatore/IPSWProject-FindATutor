package logic.model.domain;

import java.time.Instant;
import java.util.UUID;

// Rappresenta la sessione autenticata di un utente.
// Ogni sessione ha un ID univoco e un timestamp di creazione (nonostante non sia utilizzato, possiamo mantenerlo
// per un eventuale espansione della logica di autorizzazione
public class Session {

    private final UUID sessionId = UUID.randomUUID();
    private final User user;
    private final Instant createdAt = Instant.now();

    public Session(User user) {
        this.user = user;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

