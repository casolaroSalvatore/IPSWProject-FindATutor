package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

// Incarna l'AbstractState
abstract class AbstractTutoringSessionState {

    /* campi condivisi */
    protected TutoringSessionStateMachine fsm;
    protected TutoringSession             session;

    protected AbstractTutoringSessionState() {}

    protected AbstractTutoringSessionState(TutoringSessionStateMachine fsm,
                                           TutoringSession             session) {
        this.fsm     = fsm;
        this.session = session;
    }

    /* Eventi (default = invalid) ===== */
    // Use Case "Book a Tutoring Session"
    void book(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession){
        invalid("book");
    }

    void tutorAccept(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession){
        invalid("tutorAccept");
    }

    void tutorRefuse(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("tutorRefuse");
    }

    // Use Case "Manage a Notice Board"
    void requestModification(TutoringSessionStateMachine sm, TutoringSession tutoringSession,
                              LocalDate d, LocalTime s, LocalTime e,
                              String reason, String requesterId) {
        invalid("requestModification");
    }

    void acceptModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("acceptModification");
    }

    void refuseModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("refuseModification");
    }

    void requestCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession,
                              String reason, String requesterId) {
        invalid("requestCancellation");
    }

    void acceptCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("acceptCancellation");
    }

    void refuseCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        invalid("refuseCancellation");
    }

    /* ===== hook di ciclo‑vita (opzionali) ===== */
    void onEntry(TutoringSession tutoringSession) { }
    void onExit (TutoringSession tutoringSession) { }

    // Utilities
    protected final void setStatus (TutoringSession tutoringSession,
                                    TutoringSessionStatus tutoringSessionStatus){
        tutoringSession.changeStatus(tutoringSessionStatus);
    }

    private void invalid (String evt) {
        throw new IllegalStateException(evt +" is not legal in "+getClass().getSimpleName());
    }
}

