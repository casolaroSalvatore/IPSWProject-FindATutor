package logic.model.domain.state;

// Interfaccia tramite cui separiamo il Client (TutoringSession) dalla StateMachine, per evitare che il primo
// decida di sua spontanea volontà di configurare lui stesso uno stato, non rispettando così il protocollo di
// iterazione della StateMachine.

import java.time.LocalDate;
import java.time.LocalTime;

public interface TutoringSessionEvents {

    // Use Case "Book a Tutoring Session"

    // Evento che rappresenta l'invio della richiesta di prenotazione da parte dello studente.
    void book();

    // Evento che rappresenta l'accettazione della richiesta da parte del tutor.
    void tutorAccept();

    // Evento che rappresenta il rifiuto della richiesta da parte del tutor.
    void tutorRefuse();

    // Use Case "Manage a Notice Board"
    // Evento in cui uno degli utenti propone una modifica alla sessione (data/orario).
    void requestModification(LocalDate newDate,
                             LocalTime newStart,
                             LocalTime newEnd,
                             String reason,
                             String requesterId);

    // Evento in cui la controparte accetta la modifica proposta.
    void acceptModification();

    // Evento in cui la controparte rifiuta la modifica proposta.
    void refuseModification();

    // Evento in cui uno degli utenti propone la cancellazione della sessione.
    void requestCancellation(String reason, String requesterId);

    // Evento in cui la controparte accetta la cancellazione proposta.
    void acceptCancellation();

    // Evento in cui la controparte rifiuta la cancellazione proposta.
    void refuseCancellation();
}
