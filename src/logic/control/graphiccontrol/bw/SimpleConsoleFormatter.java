package logic.control.graphiccontrol.bw;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

// Formatter personalizzato per il logger: mostra solo il messaggio senza metadati aggiuntivi
public class SimpleConsoleFormatter extends Formatter {

    @Override
    // Formatter personalizzato per il logger: mostra solo il messaggio senza metadati aggiuntivi
    public String format(LogRecord logRecord) {
        return logRecord.getMessage() + System.lineSeparator();
    }
}
