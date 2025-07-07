package logic.control.graphiccontrol.colored;

// Interfaccia che rappresenta un controller "genitore" capace di aggiornare calendario e tabella
public interface BookingSessionParent {

    // Metodo che permette di aggiornare il calendario e la tabella nella scena padre
    // L'ho introdotto per eliminare l'errore "This class is part of one cycle containing
    // 2 classes within package logic.control.graphiccontrol.colored." da SonarQube
    void refreshCalendarAndTable();
}
