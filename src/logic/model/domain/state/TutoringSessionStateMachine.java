package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

// Implementa la TutoringSessionEvents e funge da state machine (Context) per una TutoringSession.
// Incapsula lo stato corrente (current) e delega tutti gli eventi all’oggetto stato attivo (ConcreteState).
// È responsabile della transizione tra stati e della sincronizzazione con l’entità domain.

public class TutoringSessionStateMachine implements TutoringSessionEvents {

    // Stato corrente della sessione (ConcreteState attivo)
    private AbstractTutoringSessionState current;

    // Riferimento al contesto Client (la sessione) su cui gli stati lavorano.
    private final TutoringSession tutoringSession;

    public TutoringSessionStateMachine(TutoringSession tutoringSession) {
        this.tutoringSession = tutoringSession;
        this.current = new DraftState();
        current.onEntry(tutoringSession);
    }

    // Evento: lo studente invia la richiesta di prenotazione.
    @Override
    public void book() {
        current.book(this, tutoringSession);
    }

    // Eventi: il tutor accetta o rifiuta la richiesta.
    @Override
    public void tutorAccept() {
        current.tutorAccept(this, tutoringSession);
    }

    @Override
    public void tutorRefuse() {
        current.tutorRefuse(this, tutoringSession);
    }

    // Evento: uno degli utenti propone una modifica alla sessione.
    @Override
    public void requestModification(LocalDate d, LocalTime s, LocalTime e,
                                    String reason, String who) {
        current.requestModification(this, tutoringSession, d, s, e, reason, who);
    }

    // Evento: la controparte accetta o rifiuta la modifica.
    @Override
    public void acceptModification() {
        current.acceptModification(this, tutoringSession);
    }

    // Evento: uno degli utenti richiede la cancellazione della sessione.
    @Override
    public void refuseModification() {
        current.refuseModification(this, tutoringSession);
    }

    @Override
    public void requestCancellation(String reason, String who) {
        current.requestCancellation(this, tutoringSession, reason, who);
    }

    // Evento: la controparte accetta o rifiuta la cancellazione.
    @Override
    public void acceptCancellation() {
        current.acceptCancellation(this, tutoringSession);
    }

    @Override
    public void refuseCancellation() {
        current.refuseCancellation(this, tutoringSession);
    }

    // Metodo interno alla FSM (non accessibile al client) che esegue una transizione di stato.
    // Invoca il metodo onExit() dello stato attuale e onEntry() del nuovo stato.
    void setState(AbstractTutoringSessionState next) {
        current.onExit(tutoringSession);
        current = next;
        current.onEntry(tutoringSession);
    }

    // Metodo chiamato per inizializzare la FSM partendo da uno stato salvato (ad esempio letto da DB).
    // Ricostruisce lo stato corretto in base all’enum status e lo collega al contesto.
    public void bootstrap(TutoringSessionStatus status) {
        switch (status) {
            case DRAFT -> current = new DraftState(this, tutoringSession);
            case PENDING -> current = new PendingState(this, tutoringSession);
            case ACCEPTED -> current = new AcceptedState(this, tutoringSession);
            case MOD_REQUESTED -> current = new ModRequestedState(this, tutoringSession);
            case CANCEL_REQUESTED -> current = new CancelRequestedState(this, tutoringSession);
            case CANCELLED -> current = new CancelledState(this, tutoringSession);
            case REFUSED -> current = new RefusedState(this, tutoringSession);
            default -> throw new IllegalArgumentException("Unsupported status: " + status);
        }
    }
}

