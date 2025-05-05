package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

// Incarna un ConcreteState
class AcceptedState extends AbstractTutoringSessionState {

    public AcceptedState() {}

    public AcceptedState(TutoringSessionStateMachine fsm,
                        TutoringSession             session) {
        super(fsm, session);
    }

    @Override void requestModification(TutoringSessionStateMachine sm, TutoringSession ctx,
                                       LocalDate d, LocalTime s, LocalTime e,
                                       String reason, String who) {
        /* stessa identica logica di PendingState */
        ctx.setProposedDate(d);
        ctx.setProposedStartTime(s);
        ctx.setProposedEndTime(e);
        ctx.setComment(reason);
        ctx.setModifiedBy(who);
        ctx.setModifiedTo(who.equals(ctx.getTutorId()) ? ctx.getStudentId() : ctx.getTutorId());
        sm.setState(new ModRequestedState());
    }

    @Override void requestCancellation(TutoringSessionStateMachine sm, TutoringSession ctx,
                                       String reason, String who) {
        ctx.setComment(reason);
        ctx.setModifiedBy(who);
        ctx.setModifiedTo(who.equals(ctx.getTutorId()) ? ctx.getStudentId() : ctx.getTutorId());
        sm.setState(new CancelRequestedState(reason));
    }
}
