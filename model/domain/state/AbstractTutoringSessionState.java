package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

// Incarna l'AbstractState: definisce l'interfaccia comune per tutti gli stati
abstract class AbstractTutoringSessionState {

    // Riferimento alla state machine che gestisce lo stato corrente
    protected TutoringSessionStateMachine fsm;

    // Riferimento alla state machine che gestisce lo stato corrente
    protected TutoringSession session;

    protected AbstractTutoringSessionState() {
    }

    protected AbstractTutoringSessionState(TutoringSessionStateMachine fsm, TutoringSession session) {
        this.fsm = fsm;
        this.session = session;
    }

    // Eventi per lo Use Case "Book a Tutoring Session"

    /* IMPORTANTE: Il metodo fa parte dell’interfaccia “contrattuale” di tutti gli stati della state-machine,
    quindi non posso cambiare la firma: in alcuni stati i parametri servono, in altri no.
    Per far sparire il warning SonarQube senza alterare la semantica possiamo pensare
    di silenziare i Warning attraverso l'annotazione @SuppressWarnings("unused")
    Quindi, per sintetizzare: silenzio i Warning SonarQube in quanto i parametri stateMachine e tutoringSession
    devono rimanere perché fanno parte della firma del metodo definita nella superclasse o interfaccia.
    Anche se non vengono usati in tutti gli stati, servono per mantenere il polimorfismo e permettere alla state
    machine di invocare i metodi in modo uniforme, senza sapere in che stato si trova. */

    @SuppressWarnings("unused")
    // Evento che rappresenta la richiesta di prenotazione da parte dello studente
    void book(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("book");
    }

    @SuppressWarnings("unused")
    // Evento che rappresenta l'accettazione della sessione da parte del tutor
    void tutorAccept(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("tutorAccept");
    }

    @SuppressWarnings("unused")
    // Evento che rappresenta il rifiuto della sessione da parte del tutor
    void tutorRefuse(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("tutorRefuse");
    }

    @SuppressWarnings("unused")
    // Use Case "Manage a Notice Board"
    // Evento che rappresenta una richiesta di modifica della sessione da parte di uno dei due utenti
    void requestModification(TutoringSessionStateMachine sm, TutoringSession tutoringSession,
                             LocalDate d, LocalTime s, LocalTime e,
                             String reason, String requesterId) {
        invalid("requestModification");
    }

    @SuppressWarnings("unused")
     // Evento che rappresenta l'accettazione della proposta di modifica
    void acceptModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("acceptModification");
    }

    @SuppressWarnings("unused")
    // Evento che rappresenta il rifiuto della proposta di modifica
    void refuseModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("refuseModification");
    }

    @SuppressWarnings("unused")
    // Evento che rappresenta la richiesta di annullamento della sessione
    void requestCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession,
                             String reason, String requesterId) {
        invalid("requestCancellation");
    }

    @SuppressWarnings("unused")
    // Evento che rappresenta l'accettazione della cancellazione
    void acceptCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("acceptCancellation");
    }

    @SuppressWarnings("unused")
    // Evento che rappresenta il rifiuto della cancellazione
    void refuseCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("refuseCancellation");
    }

    // Metodo invocato automaticamente quando la sessione entra in questo stato
    void onEntry(TutoringSession tutoringSession) {
    }

    // Metodo invocato automaticamente quando la sessione esce da questo stato
    void onExit(TutoringSession tutoringSession) {
    }

    // Cambia lo stato interno (enum) della sessione in modo centralizzato
    protected final void setStatus(TutoringSession tutoringSession,
                                   TutoringSessionStatus tutoringSessionStatus) {
        tutoringSession.changeStatus(tutoringSessionStatus);
    }

    // Metodo utility per segnalare che un certo evento non è valido nello stato corrente
    private void invalid(String evt) {
        throw new IllegalStateException(evt + " is not legal in " + getClass().getSimpleName());
    }
}

