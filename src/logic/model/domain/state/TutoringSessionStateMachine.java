package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

public class TutoringSessionStateMachine implements TutoringSessionEvents {

    private AbstractTutoringSessionState current;
    private final TutoringSession tutoringSession;

    public TutoringSessionStateMachine(TutoringSession tutoringSession){
        this.tutoringSession = tutoringSession;
        this.current = new DraftState();   // Stato di partenza
        current.onEntry(tutoringSession);
    }

    // Eventi delegati allo stato corrente
    @Override public void book() { current.book(this, tutoringSession); }
    @Override public void tutorAccept() { current.tutorAccept(this, tutoringSession); }
    @Override public void tutorRefuse() { current.tutorRefuse(this, tutoringSession); }

    @Override
    public void requestModification(LocalDate d, LocalTime s, LocalTime e,
                                    String reason, String who){
        current.requestModification(this, tutoringSession, d,s,e,reason,who);
    }

    @Override
    public void acceptModification (){ current.acceptModification (this,tutoringSession); }

    @Override
    public void refuseModification (){ current.refuseModification (this,tutoringSession); }

    @Override
    public void requestCancellation(String reason, String who){
        current.requestCancellation(this, tutoringSession, reason, who);
    }

    @Override
    public void acceptCancellation (){ current.acceptCancellation (this,tutoringSession); }

    @Override
    public void refuseCancellation (){ current.refuseCancellation (this,tutoringSession); }

    // Servizio interno di modifica dello stato, NON esposto al Client
    void setState(AbstractTutoringSessionState next){
        current.onExit(tutoringSession);
        current = next;
        current.onEntry(tutoringSession);
    }

    public void bootstrap(TutoringSessionStatus status) {
        switch (status) {
            case DRAFT -> current = new DraftState(this, tutoringSession);
            case PENDING -> current = new PendingState(this, tutoringSession);
            case ACCEPTED -> current = new AcceptedState(this, tutoringSession);
            case MOD_REQUESTED -> current = new ModRequestedState(this, tutoringSession);
            case CANCEL_REQUESTED -> current = new CancelRequestedState(this, tutoringSession);
            case CANCELLED -> current = new CancelledState(this, tutoringSession);
            default -> throw new IllegalArgumentException("Unsupported status: " + status);
        }
    }
}

