package logic.control.graphiccontrol.bw;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.logging.Level;
import java.util.logging.Logger;

// Classe base per i controller CLI (versione BW), fornisce metodi di supporto per input utente e logging.
public abstract class BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(BaseCLIControllerBW.class.getName());

    // Lettore per l'input da console.
    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));

    // Configurazione del Logger
    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    protected String ask(String prompt) {
        LOGGER.info(prompt);
        try {
            String line = IN.readLine();
            return line == null ? "" : line.trim();
        } catch (IOException e) {
            LOGGER.warning("Errore di I/O durante la lettura dell’input: " + e.getMessage());
            return "";
        }
    }

    protected int askInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(ask(prompt));
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid input. Expected a number.");
                LOGGER.info("Please enter a valid number!");
            }
        }
    }

    protected LocalDate askDate(String prompt) {
        while (true) {
            try {
                return LocalDate.parse(ask(prompt + " [yyyy-MM-dd]"));
            } catch (Exception e) {
                LOGGER.warning("Invalid date format entered by user.");
                LOGGER.info("Invalid date format! Please use yyyy-MM-dd (e.g., 2025-04-17)");
            }
        }
    }

    protected LocalTime askTime(String prompt) {
        while (true) {
            try {
                return LocalTime.parse(ask(prompt + " [HH:mm]"));
            } catch (Exception e) {
                LOGGER.warning("Invalid time format entered. Expected HH:mm format.");
                LOGGER.info("Please enter a valid time! Use HH:mm (e.g., 14:30)");
            }
        }
    }

    protected void pressEnter() {
        ask("Premi INVIO per continuare...");
    }
}



