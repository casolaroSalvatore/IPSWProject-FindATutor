package logic.model.domain.state;

class CancelRequestedState extends AbstractTutoringSessionState {

    private String reason;

    CancelRequestedState(String r){ this.reason = r; }

    public CancelRequestedState(TutoringSessionStateMachine fsm,
                          TutoringSession             session) {
        super(fsm, session);
    }

    @Override void onEntry(TutoringSession ctx){
        ctx.setComment(reason);
        setStatus(ctx, TutoringSessionStatus.CANCEL_REQUESTED);
    }

    @Override void acceptCancellation(TutoringSessionStateMachine sm, TutoringSession ctx){
        setStatus(ctx, TutoringSessionStatus.CANCELLED);
        sm.setState(new CancelledState());
    }

    @Override void refuseCancellation(TutoringSessionStateMachine sm, TutoringSession ctx){
        setStatus(ctx, TutoringSessionStatus.ACCEPTED);
        sm.setState(new AcceptedState());
    }
}
