package logic.model.domain.state;

// ConcreteState di partenza di una TutoringSession, che appena creata ha questo stato
class DraftState extends AbstractTutoringSessionState {

    public DraftState() {}

    public DraftState(TutoringSessionStateMachine fsm,
                      TutoringSession             session) {
        super(fsm, session);
    }

    @Override
    void book(TutoringSessionStateMachine sm, TutoringSession ctx) {
        sm.setState(new PendingState());          // transizione
    }
}
