package logic.control.graphiccontrol.bw;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import logic.bean.*;
import logic.control.logiccontrol.BookingTutoringSessionController;
import logic.exception.NoTutorFoundException;

// Controller grafico per la versione CLI BW che gestisce la prenotazione di una sessione di tutoraggio.
public class BookingSessionGraphicControllerBW extends BaseCLIControllerBW {

    private UUID sessionId;

    private BookingTutoringSessionController logic;

    public BookingSessionGraphicControllerBW(UUID sessionId) {
        this.sessionId = sessionId;
        this.logic = new BookingTutoringSessionController(sessionId);
    }

    private static final Logger LOGGER = Logger.getLogger(BookingSessionGraphicControllerBW.class.getName());

    static {
        // Configuro il logger
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    // Avvia il processo di prenotazione
    public void start() throws NoTutorFoundException {
        LOGGER.log(Level.INFO, "\n=== BOOK A TUTORING SESSION ===");

        String subject = ask("Subject (leave empty to search all):");
        String location = ask("Location (leave empty to search all):");

        LocalDate startDate = askDate("Start Date:");
        LocalDate endDate = askDate("End Date:");

        String daysInput = ask("Days of week (ex: MON,TUE – empty = all):");
        List<DayOfWeek> days = parseDays(daysInput);

        AvailabilityBean av = new AvailabilityBean();
        av.setStartDate(startDate);
        av.setEndDate(endDate);
        av.setDays(days);

        TutorSearchCriteriaBean criteria = new TutorSearchCriteriaBean.Builder()
                .subject(subject)
                .location(location)
                .availability(av)
                .inPerson(false)
                .online(false)
                .group(false)
                .rating4Plus(false)
                .firstLessonFree(false)
                .orderCriteria(null)
                .build();

        // Cerca tutor disponibili con i criteri forniti
        List<TutorBean> tutors = logic.searchTutors(criteria);

        if (tutors.isEmpty()) {
            LOGGER.info("No tutors found.");
            pressEnter();
            return;
        }

        // Mostra la lista dei tutor trovati
        LOGGER.info("\nAvailable Tutors:");
        IntStream.range(0, tutors.size()).forEach(i -> {
            TutorBean t = tutors.get(i);

            // Calcola i giorni prenotabili tra quelli richiesti e quelli del tutor
            List<DayBookingBean> availableDays = logic.computeDayBookingsForTutor(
                    t.getAccountId(), criteria.getAvailability());

            // Crea stringa dei giorni (es: MON, TUE)
            String daysStr = "N/A";
            if (!availableDays.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (DayBookingBean d : availableDays) {
                    if (!sb.isEmpty()) sb.append(",");
                    sb.append(d.getDate());
                }
                daysStr = sb.toString();
            }

            String tutorInfo = String.format(
                    "%2d) %s %s – Subject: %s – Hourly Rate: €%.2f – Rating: %.1f – Available Days: %s",
                    i + 1, t.getName(), t.getSurname(),
                    Optional.ofNullable(t.getSubject()).orElse("N/A"),
                    t.getHourlyRate(), t.getRating(), daysStr
            );
            LOGGER.log(Level.INFO, tutorInfo);
        });

        // Chiede all'utente quale tutor selezionare
        int choice = askInt("Select tutor number (0 to cancel):") - 1;
        if (choice < 0 || choice >= tutors.size()) {
            return;
        }
        TutorBean selectedTutor = tutors.get(choice);

        if (logic.getLoggedUser(sessionId) == null) {
            LOGGER.info("Please log in first!");
            pressEnter();
            return;
        }

        boolean keepAsking = true;
        while (keepAsking) {
            LOGGER.info("\n[V] View tutor profile   [C] Continue to booking   [0] Cancel");
            String in = ask("Choose:").trim().toUpperCase();

            switch (in) {
                case "0":
                    return;
                case "V":
                    new TutorProfileGraphicControllerBW().show(selectedTutor.getAccountId());
                    break;
                case "C":
                    keepAsking = false; // esci dal ciclo e procedi
                    break;
                default:
                    LOGGER.info("Invalid input. Please choose one of the available options.");
                    break;
            }
        }

        LocalDate sessionDate = askDate("Enter the date for the session:");
        LocalTime startTime = askTime("Enter the start time:");
        LocalTime endTime = askTime("Enter the end time:");
        String comment = ask("Comment (optional):");

        // Costruisce il bean e invia la prenotazione
        try {

            TutoringSessionBean bean = logic.buildBookingBean(
                    selectedTutor,
                    sessionDate, startTime, endTime,
                    location, subject, comment);

            logic.bookSession(bean);
            LOGGER.info("Booking sent successfully! Please wait for the tutor's confirmation.");
        } catch (IllegalArgumentException ex) {
            LOGGER.warning("Booking error: " + ex.getMessage());
        }

        pressEnter();
    }

    // Supporta sia abbreviazioni (MON, TUE) che nomi completi (MONDAY, TUESDAY)
    private static final Map<String, DayOfWeek> DAY_ALIASES = Map.ofEntries(
            Map.entry("MON", DayOfWeek.MONDAY),
            Map.entry("TUE", DayOfWeek.TUESDAY),
            Map.entry("WED", DayOfWeek.WEDNESDAY),
            Map.entry("THU", DayOfWeek.THURSDAY),
            Map.entry("FRI", DayOfWeek.FRIDAY),
            Map.entry("SAT", DayOfWeek.SATURDAY),
            Map.entry("SUN", DayOfWeek.SUNDAY)
    );

    // Converte una stringa tipo "MON,TUE" in una lista di DayOfWeek.
    private List<DayOfWeek> parseDays(String input) {
        List<DayOfWeek> result = new ArrayList<>();
        if (input == null || input.isBlank()) return result;

        String[] parts = input.split(",");
        for (String part : parts) {
            String token = part.trim().toUpperCase();
            if (token.isEmpty()) continue;

            DayOfWeek day;
            if (DAY_ALIASES.containsKey(token)) {
                day = DAY_ALIASES.get(token);
            } else {
                try {
                    day = DayOfWeek.valueOf(token);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid day of week: '" + token + "'. Valid values: MON, TUE, ..., SUNDAY");
                }
            }
            result.add(day);
        }
        return result;
    }
}


