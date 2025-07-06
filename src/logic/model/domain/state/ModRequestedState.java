package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

class ModRequestedState extends AbstractTutoringSessionState {

    public ModRequestedState() {}

    public ModRequestedState(TutoringSessionStateMachine fsm,
                          TutoringSession             session) {
        super(fsm, session);
    }


    @Override void onEntry(TutoringSession tutoringSession){
        setStatus(tutoringSession, TutoringSessionStatus.MOD_REQUESTED);
    }

    @Override
    void acceptModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession){
        tutoringSession.setDate(tutoringSession.getProposedDate());
        tutoringSession.setStartTime(tutoringSession.getProposedStartTime());
        tutoringSession.setEndTime(tutoringSession.getProposedEndTime());
        tutoringSession.setStatus(TutoringSessionStatus.ACCEPTED);
        stateMachine.setState(new AcceptedState());
    }

    @Override
    void refuseModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession){
        tutoringSession.setStatus(TutoringSessionStatus.ACCEPTED);
        stateMachine.setState(new AcceptedState());
    }
}
