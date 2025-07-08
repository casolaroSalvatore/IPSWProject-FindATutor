package logic.model.domain.state;

// ConcreteState che rappresenta una sessione rifiutata dal tutor.
// Si tratta di uno stato terminale: non sono previste transizioni successive da qui.
public class RefusedState extends AbstractTutoringSessionState {

    public RefusedState() {}

    public RefusedState(TutoringSessionStateMachine stateMachine, TutoringSession session) {
        onEntry(session);
    }

    @Override
    public void onEntry(TutoringSession session) {
        session.setStatus(TutoringSessionStatus.REFUSED);
    }

    @Override
    public void onExit(TutoringSession session) {
        // Nessuna azione necessaria
    }

    // Blocca tutti gli eventi perché una sessione rifiutata è "morta"
    @Override
    public void book(TutoringSessionStateMachine sm, TutoringSession s) {
        throw new IllegalStateException("Cannot book a refused session");
    }

    @Override
    public void tutorAccept(TutoringSessionStateMachine sm, TutoringSession s) {
        throw new IllegalStateException("Cannot accept a refused session");
    }

    @Override
    public void tutorRefuse(TutoringSessionStateMachine sm, TutoringSession s) {
        throw new IllegalStateException("Already refused");
    }

    @Override
    public void requestModification(TutoringSessionStateMachine sm, TutoringSession s,
                                    java.time.LocalDate d, java.time.LocalTime start, java.time.LocalTime end,
                                    String reason, String who) {
        throw new IllegalStateException("Cannot modify a refused session");
    }

    @Override
    public void acceptModification(TutoringSessionStateMachine sm, TutoringSession s) {
        throw new IllegalStateException("Cannot accept modification on a refused session");
    }

    @Override
    public void refuseModification(TutoringSessionStateMachine sm, TutoringSession s) {
        throw new IllegalStateException("Cannot refuse modification on a refused session");
    }

    @Override
    public void requestCancellation(TutoringSessionStateMachine sm, TutoringSession s, String reason, String who) {
        throw new IllegalStateException("Cannot request cancellation on a refused session");
    }

    @Override
    public void acceptCancellation(TutoringSessionStateMachine sm, TutoringSession s) {
        throw new IllegalStateException("Cannot accept cancellation on a refused session");
    }

    @Override
    public void refuseCancellation(TutoringSessionStateMachine sm, TutoringSession s) {
        throw new IllegalStateException("Cannot refuse cancellation on a refused session");
    }
}
