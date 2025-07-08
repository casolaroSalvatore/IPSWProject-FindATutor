package logic.control.graphiccontrol.bw;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        LOGGER.info("\n=== BOOK A TUTORING SESSION ===");

        /* ---- input di ricerca ------------------------------------- */
        String subject  = ask("Subject (leave empty to search all):");
        String location = ask("Location (leave empty to search all):");
        LocalDate start = askDate("Start Date:");
        LocalDate end   = askDate("End Date:");
        List<DayOfWeek> days = parseDays(
                ask("Days of week (ex: MON,TUE – empty = all):"));

        TutorSearchCriteriaBean criteria = buildCriteria(
                subject, location, start, end, days);

        List<TutorBean> tutors = logic.searchTutors(criteria);
        if (tutors.isEmpty()) { LOGGER.info("No tutors found."); pressEnter(); return; }

        showTutors(tutors, criteria);

        TutorBean chosen = chooseTutor(tutors);
        if (chosen == null) return;
        if (logic.getLoggedUser(sessionId) == null) {
            LOGGER.info("Please log in first!");
            pressEnter(); return;
        }

        doBooking(chosen, location, subject);
        pressEnter();
    }

    // Helper per diminuire la complessità (richiesto da SonarQube

    // Costruisce i criteri di ricerca a partire dai dati inseriti */
    private TutorSearchCriteriaBean buildCriteria(String subject, String location,
                                                  LocalDate start, LocalDate end,
                                                  List<DayOfWeek> days) {

        AvailabilityBean av = new AvailabilityBean();
        av.setStartDate(start);
        av.setEndDate(end);
        av.setDays(days);

        return new TutorSearchCriteriaBean.Builder()
                .subject(subject)
                .location(location)
                .availability(av)
                .inPerson(false).online(false).group(false)
                .rating4Plus(false).firstLessonFree(false)
                .orderCriteria(null)
                .build();
    }

    // Stampa la lista dei tutor con i giorni prenotabili
    private void showTutors(List<TutorBean> list, TutorSearchCriteriaBean crit) {

        LOGGER.info("\nAvailable Tutors:");
        for (int i = 0; i < list.size(); i++) {
            TutorBean t = list.get(i);

            List<DayBookingBean> days =
                    logic.computeDayBookingsForTutor(t.getAccountId(), crit.getAvailability());

            StringBuilder sb = new StringBuilder();
            for (DayBookingBean d : days) {
                if (!sb.isEmpty()) sb.append(",");
                sb.append(d.getDate());
            }
            String daysStr = sb.isEmpty() ? "N/A" : sb.toString();

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, String.format(
                        "%2d) %s %s – Subject: %s – €%.2f/h – ★%.1f – Days: %s",
                        i + 1, t.getName(), t.getSurname(),
                        Optional.ofNullable(t.getSubject()).orElse("N/A"),
                        t.getHourlyRate(), t.getRating(), daysStr));
            }
        }
    }

    // Chiede all’utente quale tutor vuole selezionare (o 0 per annullare)
    private TutorBean chooseTutor(List<TutorBean> tutors) {
        int idx = askInt("Select tutor number (0 to cancel):") - 1;
        return (idx < 0 || idx >= tutors.size()) ? null : tutors.get(idx);
    }

    // Raccoglie i dati della sessione e invia la prenotazione
    private void doBooking(TutorBean tutor, String location, String subject) {

        LocalDate date   = askDate("Enter the date for the session:");
        LocalTime start  = askTime("Enter the start time:");
        LocalTime end    = askTime("Enter the end time:");
        String comment   = ask("Comment (optional):");

        try {
            TutoringSessionBean bean = logic.buildBookingBean(
                    tutor, date, start, end, location, subject, comment);

            logic.bookSession(bean);
            LOGGER.info("Booking sent successfully! Please wait for the tutor's confirmation.");
        } catch (IllegalArgumentException ex) {
            LOGGER.warning("Booking error: " + ex.getMessage());
        }
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


