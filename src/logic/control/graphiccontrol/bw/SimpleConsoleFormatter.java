package logic.control.graphiccontrol.bw;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleConsoleFormatter extends Formatter {
    @Override
    public String format(LogRecord logRecord) {
        return logRecord.getMessage() + System.lineSeparator();
    }
}
