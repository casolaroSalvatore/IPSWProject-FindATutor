package logic.control.graphiccontrol.bw;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import logic.bean.AvailabilityBean;
import logic.bean.TutorBean;
import logic.bean.TutorSearchCriteriaBean;
import logic.bean.TutoringSessionBean;
import logic.control.logiccontrol.BookingTutoringSessionController;
import logic.exception.NoTutorFoundException;

public class BookingSessionGraphicControllerBW extends BaseCLIControllerBW {

    private UUID sessionId;

    public BookingSessionGraphicControllerBW(UUID sessionId) {
        this.sessionId = sessionId;
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

    private final BookingTutoringSessionController logic = new BookingTutoringSessionController();

    public void start() throws NoTutorFoundException {
        LOGGER.log(Level.INFO, "\n=== BOOK A TUTORING SESSION ===");

        String subject = ask("Subject (leave empty to search all):");
        String location = ask("Location (leave empty to search all):");

        LocalDate startDate = askDate("Start Date:");
        LocalDate endDate   = askDate("End Date:");

        AvailabilityBean av = new AvailabilityBean();
        av.setStartDate(startDate);
        av.setEndDate(endDate);

        // Costruisco l'oggetto criteria necessario per la chiamata di searchTutor
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

        List<TutorBean> tutors = logic.searchTutors(criteria);

        if (tutors.isEmpty()) {
            LOGGER.info("No tutors found.");
            pressEnter();
            return;
        }

        LOGGER.info("\nAvailable Tutors:");
        IntStream.range(0, tutors.size()).forEach(i -> {
            TutorBean t = tutors.get(i);
            String tutorInfo = String.format("%2d) %s %s – Subject: %s – Hourly Rate: €%.2f – Rating: %.1f",
                    i + 1, t.getName(), t.getSurname(),
                    Optional.ofNullable(t.getSubject()).orElse("N/A"), t.getHourlyRate(), t.getRating());
            LOGGER.log(Level.INFO, tutorInfo);
        });

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

        try {

            logic.getStudentAccountId();

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
}


