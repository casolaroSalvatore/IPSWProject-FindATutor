package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

// ConcreteState che rappresenta una sessione di tutoraggio in attesa di risposta da parte del tutor.
// Da questo stato è possibile accettare o rifiutare la richiesta, oppure proporre una modifica o una cancellazione.
class PendingState extends AbstractTutoringSessionState {

    public PendingState() {
    }

    public PendingState(TutoringSessionStateMachine fsm, TutoringSession session) {
        super(fsm, session);
    }

    // Evento: accettazione della sessione da parte del tutor.
    // Cambia lo stato logico a ACCEPTED e transita allo stato AcceptedState.
    @Override
    void tutorAccept(TutoringSessionStateMachine sm, TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.ACCEPTED);
        sm.setState(new AcceptedState());
    }

    // Evento: rifiuto della sessione da parte del tutor.
    // Cambia lo stato logico a REFUSED e transita allo stato RefusedState.
    @Override
    void tutorRefuse(TutoringSessionStateMachine sm, TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.REFUSED);
        sm.setState(new RefusedState());
    }

    // Eseguito all'ingresso nello stato: imposta lo stato logico della sessione su PENDING.
    @Override
    void onEntry(TutoringSession ctx) {
        setStatus(ctx, TutoringSessionStatus.PENDING);
    }

    // Evento: richiesta di modifica della sessione (data/orario) da parte di uno degli utenti.
    // Registra la proposta, il richiedente e il destinatario, poi transita a ModRequestedState.
    @Override
    void requestModification(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession,
                             LocalDate d, LocalTime s, LocalTime e,
                             String reason, String requesterId) {

        tutoringSession.setProposedDate(d);
        tutoringSession.setProposedStartTime(s);
        tutoringSession.setProposedEndTime(e);
        tutoringSession.setComment(reason);
        tutoringSession.setModifiedBy(requesterId);
        tutoringSession.setModifiedTo(requesterId.equals(tutoringSession.getTutorId()) ? tutoringSession.getStudentId()
                : tutoringSession.getTutorId());
        stateMachine.setState(new ModRequestedState());
    }

    // Evento: richiesta di cancellazione da parte di uno degli utenti.
    // Registra il motivo, il richiedente e il destinatario, poi transita a CancelRequestedState.
    @Override
    void requestCancellation(TutoringSessionStateMachine stateMachine, TutoringSession tutoringSession,
                             String reason, String requesterId) {
        tutoringSession.setComment(reason);
        tutoringSession.setModifiedBy(requesterId);
        tutoringSession.setModifiedTo(requesterId.equals(tutoringSession.getTutorId()) ? tutoringSession.getStudentId()
                : tutoringSession.getTutorId());
        stateMachine.setState(new CancelRequestedState(reason));
    }
}
