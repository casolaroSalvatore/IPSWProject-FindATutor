package logic.control.graphiccontrol.bw;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

// Handler personalizzato per il logger che scrive su System.out con un formato semplice
public class SystemOutConsoleHandler extends StreamHandler {

    // Costruttore: inizializza lo StreamHandler per scrivere su System.out
    // e applica il SimpleConsoleFormatter per formattare i messaggi
    public SystemOutConsoleHandler() {
        super(System.out, new SimpleConsoleFormatter());
        setLevel(Level.ALL);
    }

    // Pubblica un record di log e forza la scrittura immediata su console
    @Override
    public synchronized void publish(LogRecord logRecord) {
        super.publish(logRecord);

        // Forza la scrittura immediata del messaggio
        flush();
    }
}
