package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

// Incarna un ConcreteState
class PendingState extends AbstractTutoringSessionState {

    public PendingState() {}

    public PendingState(TutoringSessionStateMachine fsm,
                      TutoringSession             session) {
        super(fsm, session);
    }

    @Override
    void tutorAccept(TutoringSessionStateMachine sm, TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.ACCEPTED);
        sm.setState(new AcceptedState());
    }
    @Override
    void tutorRefuse(TutoringSessionStateMachine sm, TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.REFUSED);
        sm.setState(new RefusedState());
    }

    @Override
    void onEntry(TutoringSession ctx){ setStatus(ctx, TutoringSessionStatus.PENDING); }

    @Override
    void requestModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession,
                             LocalDate d, LocalTime s, LocalTime e,
                             String reason, String requesterId){

        tutoringSession.setProposedDate(d);
        tutoringSession.setProposedStartTime(s);
        tutoringSession.setProposedEndTime(e);
        tutoringSession.setComment(reason);
        tutoringSession.setModifiedBy(requesterId);
        tutoringSession.setModifiedTo(requesterId.equals(tutoringSession.getTutorId()) ? tutoringSession.getStudentId()
                : tutoringSession.getTutorId());
        stateMachine.setState(new ModRequestedState());
    }

    @Override
    void requestCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession,
                             String reason, String requesterId){
        tutoringSession.setComment(reason);
        tutoringSession.setModifiedBy(requesterId);
        tutoringSession.setModifiedTo(requesterId.equals(tutoringSession.getTutorId()) ? tutoringSession.getStudentId()
                : tutoringSession.getTutorId());
        stateMachine.setState(new CancelRequestedState(reason));
    }
}
