package logic.control.graphiccontrol.bw;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class SystemOutConsoleHandler extends StreamHandler {

    public SystemOutConsoleHandler() {
        super(System.out, new SimpleConsoleFormatter());
        setLevel(Level.ALL);
    }

    @Override
    public synchronized void publish(LogRecord logRecord) {
        super.publish(logRecord);
        flush();
    }
}
