package logic.model.domain.state;

// Enum che rappresenta lo stato logico persistente di una TutoringSession.
public enum TutoringSessionStatus {
    DRAFT,             // La sessione è in bozza (non ancora inviata)
    PENDING,           // In attesa di risposta da parte del tutor
    ACCEPTED,          // Accettata dal tutor
    REFUSED,           // Rifiutata dal tutor
    MOD_REQUESTED,     // Modifica proposta da uno degli utenti, in attesa di risposta
    CANCEL_REQUESTED,  // Cancellazione proposta, in attesa di risposta
    CANCELLED          // Sessione annullata
}
