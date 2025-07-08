package logic.model.domain.state;

import java.time.LocalDate;
import java.time.LocalTime;

// Classe Client del pattern State. Rappresenta una sessione di tutoraggio tra uno studente e un tutor.
// La logica dei cambiamenti di stato viene delegata alla FSM (TutoringSessionStateMachine),
// mentre questa classe mantiene i dati e espone un’interfaccia ad alto livello per i cambiamenti di stato.
public class TutoringSession {

    private String sessionId;
    private String tutorId;
    private String studentId;
    private String location;
    private String subject;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String comment;
    private TutoringSessionStatus status;
    // Istanziazione della state machine associata alla sessione.
    // È l’oggetto che implementa le transizioni tra stati (Context del pattern GoF State).
    private TutoringSessionEvents fsm = new TutoringSessionStateMachine(this);
    private String modifiedBy;
    private String modifiedTo;
    private LocalDate proposedDate;
    private LocalTime proposedStartTime;
    private LocalTime proposedEndTime;

    // Booleani per implementare l'animazione della notifica
    private boolean tutorSeen;
    private boolean studentSeen;

    public TutoringSession() {
    }

    public TutoringSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public TutoringSession(String tutorId, String studentId, LocalDate date,
                           LocalTime startTime, LocalTime endTime,
                           String comment, TutoringSessionStatus status) {
        this.tutorId = tutorId;
        this.studentId = studentId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.comment = comment;
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTutorId() {
        return tutorId;
    }

    public void setTutorId(String tutorId) {
        this.tutorId = tutorId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public TutoringSessionStatus getStatus() {
        return status;
    }

    /* Questo metodo è utilizzato ESCLUSIVAMENTE dai DAO per ripristinare lo stato letto dal DB/file.
     Non passa dalla FSM, quindi va chiamato SOLO in fase di load. */
    public void restoreStatusFromPersistence(TutoringSessionStatus status) {
        this.status = status;
        if (fsm instanceof TutoringSessionStateMachine sm) {
            sm.bootstrap(status);
        }
    }

    void setStatus(TutoringSessionStatus status) {
        this.status = status;
    }

    // Metodo necessario per il pattern GoF State
    public void changeStatus(TutoringSessionStatus status) {
        this.status = status;
    }


    // Invoca l’evento book() sulla FSM. Utilizzato quando la sessione passa da Draft a Pending.
    public void book() {
        fsm.book();
    }

    // Evento invocato dal tutor per accettare la richiesta. Delega alla FSM.
    public void tutorAccept() {
        fsm.tutorAccept();
    }

    // Evento invocato dal tutor per rifiutare la richiesta. Delega alla FSM.
    public void tutorRefuse() {
        fsm.tutorRefuse();
    }

    // Evento invocato da uno dei due utenti per proporre una modifica (data/orario).
    // Delega l’operazione alla FSM, che applica la transizione corretta.
    public void askModification(LocalDate d, LocalTime s, LocalTime e,
                                String reason, String requesterId) {
        fsm.requestModification(d, s, e, reason, requesterId);
    }

    // Permette di accettare o rifiutare una modifica proposta, delegando alla FSM.
    // Se accept è true --> accetta, altrimenti rifiuta.
    public void respondModification(boolean accept) {
        if (accept) fsm.acceptModification();
        else fsm.refuseModification();
    }

    // Evento invocato da uno degli utenti per richiedere la cancellazione della sessione.
    public void askCancellation(String reason, String requesterId) {
        fsm.requestCancellation(reason, requesterId);
    }

    // Permette di accettare o rifiutare la richiesta di cancellazione.
    public void respondCancellation(boolean accept) {
        if (accept) fsm.acceptCancellation();
        else fsm.refuseCancellation();
    }

    public boolean isTutorSeen() {
        return tutorSeen;
    }

    public void setTutorSeen(boolean tutorSeen) {
        this.tutorSeen = tutorSeen;
    }

    public boolean isStudentSeen() {
        return studentSeen;
    }

    public void setStudentSeen(boolean studentSeen) {
        this.studentSeen = studentSeen;
    }

    public LocalDate getProposedDate() {
        return proposedDate;
    }

    public void setProposedDate(LocalDate date) {
        this.proposedDate = date;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public String getModifiedTo() {
        return modifiedTo;
    }

    public void setModifiedTo(String modifiedTo) {
        this.modifiedTo = modifiedTo;
    }

    public LocalTime getProposedStartTime() {
        return proposedStartTime;
    }

    public void setProposedStartTime(LocalTime startTime) {
        this.proposedStartTime = startTime;
    }

    public LocalTime getProposedEndTime() {
        return proposedEndTime;
    }

    public void setProposedEndTime(LocalTime endTime) {
        this.proposedEndTime = endTime;
    }
}

