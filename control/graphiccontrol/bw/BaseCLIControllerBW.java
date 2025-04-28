package logic.control.graphiccontrol.bw;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(BaseCLIControllerBW.class.getName());
    protected static final Scanner IN = new Scanner(System.in);

    static {
        // Configuro il logger per stampare su console (per rendere il codice SonarQube-compliant)
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    protected String ask(String prompt) {
        LOGGER.info(prompt);
        return IN.nextLine().trim();
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
        ask("Press ENTER to continue...");
    }
}


