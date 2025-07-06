package logic.model.domain.state;

class CancelledState extends AbstractTutoringSessionState {

    public CancelledState() {}

    public CancelledState(TutoringSessionStateMachine fsm,
                         TutoringSession             session) {
        super(fsm, session);
    }

    @Override void onEntry(TutoringSession ctx){
        setStatus(ctx, TutoringSessionStatus.CANCELLED);
    }
}
