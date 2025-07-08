package logic.model.domain.state;

// Incarna un ConcreteState: rappresenta lo stato "CancelRequested" della sessione di tutoraggio
class CancelRequestedState extends AbstractTutoringSessionState {

    private String reason;

    CancelRequestedState(String r) {
        this.reason = r;
    }

    public CancelRequestedState(TutoringSessionStateMachine fsm, TutoringSession session) {
        super(fsm, session);
    }

    // Invocato all'ingresso nello stato: imposta il motivo della cancellazione
    // e lo stato logico su CANCEL_REQUESTED.
    @Override
    void onEntry(TutoringSession ctx) {
        ctx.setComment(reason);
        setStatus(ctx, TutoringSessionStatus.CANCEL_REQUESTED);
    }

    // Evento: accettazione della richiesta di cancellazione.
    // Imposta lo stato su CANCELLED e passa a CancelledState.
    @Override
    void acceptCancellation(TutoringSessionStateMachine sm, TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.CANCELLED);
        sm.setState(new CancelledState());
    }

    // Evento: rifiuto della cancellazione.
    // Riporta lo stato logico su ACCEPTED e torna in AcceptedState.
    @Override
    void refuseCancellation(TutoringSessionStateMachine sm, TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.ACCEPTED);
        sm.setState(new AcceptedState());
    }
}
