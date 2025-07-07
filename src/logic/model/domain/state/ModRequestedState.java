package logic.model.domain.state;

// ConcreteState che rappresenta una sessione in cui è stata richiesta una modifica
// (data e/o orario) da uno degli utenti.
class ModRequestedState extends AbstractTutoringSessionState {

    public ModRequestedState() {
    }

    public ModRequestedState(TutoringSessionStateMachine fsm,
                             TutoringSession session) {
        super(fsm, session);
    }

    // Invocato all'ingresso nello stato: aggiorna lo stato logico della sessione a MOD_REQUESTED.
    @Override
    void onEntry(TutoringSession tutoringSession) {
        setStatus(tutoringSession, TutoringSessionStatus.MOD_REQUESTED);
    }

    // Evento: accettazione della proposta di modifica.
    // Aggiorna la sessione con i nuovi valori proposti e transita allo stato Accepted.
    @Override
    void acceptModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        tutoringSession.setDate(tutoringSession.getProposedDate());
        tutoringSession.setStartTime(tutoringSession.getProposedStartTime());
        tutoringSession.setEndTime(tutoringSession.getProposedEndTime());
        tutoringSession.setStatus(TutoringSessionStatus.ACCEPTED);
        stateMachine.setState(new AcceptedState());
    }

    // Evento: rifiuto della proposta di modifica.
    // Riporta lo stato logico su ACCEPTED e torna allo stato Accepted.
    @Override
    void refuseModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession) {
        tutoringSession.setStatus(TutoringSessionStatus.ACCEPTED);
        stateMachine.setState(new AcceptedState());
    }
}
