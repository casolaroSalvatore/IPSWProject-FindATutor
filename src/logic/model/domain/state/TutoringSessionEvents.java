package logic.model.domain.state;

// Interfaccia tramite cui separiamo il Client (TutoringSession) dalla StateMachine, per evitare che il primo
// decida di sua spontanea volontà di configurare lui stesso uno stato, non rispettando così il protocollo di
// iterazione della StateMachine.

import java.time.LocalDate;
import java.time.LocalTime;

public interface TutoringSessionEvents {

    // Use Case "Book a Tutoring Session"
    void book();
    void tutorAccept();
    void tutorRefuse();

    // Use Case "Manage a Notice Board"
    void requestModification(LocalDate newDate,
                             LocalTime newStart,
                             LocalTime newEnd,
                             String reason,
                             String requesterId);

    void acceptModification();           // sempre la controparte
    void refuseModification();

    void requestCancellation(String reason, String requesterId);
    void acceptCancellation();
    void refuseCancellation();
}
