package logic.model.domain.state;

// Incarna un ConcreteState: rappresenta lo stato "Canceled" della sessione di tutoraggio
class CancelledState extends AbstractTutoringSessionState {

    public CancelledState() {
    }

    public CancelledState(TutoringSessionStateMachine fsm, TutoringSession session) {
        super(fsm, session);
    }

    // Invocato all'ingresso nello stato: imposta lo stato logico della sessione su CANCELLED.
    @Override
    void onEntry(TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.CANCELLED);
    }
}
