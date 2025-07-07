package logic.control.graphiccontrol.colored;

import logic.bean.UserBean;
import java.util.UUID;

// Interfaccia per controller grafici che devono inizializzare dati di sessione e utente
public interface NavigableController {

    // Metodo da implementare per ricevere sessione e utente corrente
    // L'ho introdotto per eliminare l'errore "This class is part of one cycle containing
    // 6 classes within package logic.control.graphiccontrol.colored." da SonarQube
    void initData(UUID sessionId, UserBean userBean);
}
