package logic.control.graphiccontrol.bw;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import logic.bean.AccountBean;
import logic.bean.TutoringSessionBean;
import logic.control.logiccontrol.BookingTutoringSessionController;
import logic.model.domain.*;
import logic.model.dao.AccountDAO;
import logic.model.dao.DaoFactory;

public class BookingSessionGraphicControllerBW extends BaseCLIControllerBW {

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
    private final AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

    public void start() {
        LOGGER.log(Level.INFO, "\n=== BOOK A TUTORING SESSION ===");

        String subject = ask("Subject (leave empty to search all):");
        String location = ask("Location (leave empty to search all):");

        askDate("Start Date:");
        askDate("End Date:");

        List<Tutor> tutors = accountDAO.loadAllAccountsOfType("Tutor").stream()
                .map(a -> (Tutor) a)
                .filter(t -> (subject.isBlank() || subject.equalsIgnoreCase(t.getSubject()))
                        && (location.isBlank() || location.equalsIgnoreCase(t.getLocation())))
                .toList();

        if (tutors.isEmpty()) {
            LOGGER.info("No tutors found.");
            pressEnter();
            return;
        }

        LOGGER.info("\nAvailable Tutors:");
        IntStream.range(0, tutors.size()).forEach(i -> {
            Tutor t = tutors.get(i);
            String tutorInfo = String.format("%2d) %s %s – Subject: %s – Hourly Rate: €%.2f – Rating: %.1f",
                    i + 1, t.getName(), t.getSurname(),
                    Optional.ofNullable(t.getSubject()).orElse("N/A"), t.getHourlyRate(), t.getRating());
            LOGGER.log(Level.INFO, tutorInfo);
        });

        int choice = askInt("Select tutor number (0 to cancel):") - 1;
        if (choice < 0 || choice >= tutors.size()) {
            return;
        }
        Tutor selectedTutor = tutors.get(choice);

        if (SessionManager.getLoggedUser() == null) {
            LOGGER.info("Please log in first!");
            pressEnter();
            return;
        }

        LocalDate sessionDate = askDate("Enter the date for the session:");
        LocalTime startTime = askTime("Enter the start time:");
        LocalTime endTime = askTime("Enter the end time:");
        String comment = ask("Comment (optional):");

        TutoringSessionBean bean = new TutoringSessionBean();
        bean.setTutorId(selectedTutor.getAccountId());

        // Cerco manualmente l'account Studente
        String studentId = null;
        for (AccountBean account : SessionManager.getLoggedUser().getAccounts()) {
            if ("Student".equalsIgnoreCase(account.getRole())) {
                studentId = account.getAccountId();
                break;
            }
        }

        if (studentId == null) {
            throw new IllegalStateException("No student account found for the logged user.");
        }

        bean.setStudentId(studentId);
        bean.setDate(sessionDate);
        bean.setStartTime(startTime);
        bean.setEndTime(endTime);
        bean.setLocation(location);
        bean.setSubject(subject);
        bean.setComment(comment);

        logic.bookSession(bean);

        LOGGER.info("Booking sent successfully! Please wait for the tutor's confirmation.");
        pressEnter();
    }
}


