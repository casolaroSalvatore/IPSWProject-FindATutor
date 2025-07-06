package logic.control.graphiccontrol.bw;

import logic.bean.AuthResultBean;
import logic.bean.AvailabilityBean;
import logic.bean.AccountBean;
import logic.bean.UserBean;
import logic.control.logiccontrol.SignUpController;
import logic.exception.NoTutorFoundException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignUpGraphicControllerBW extends BaseCLIControllerBW {

    private static final Logger LOGGER = Logger.getLogger(SignUpGraphicControllerBW.class.getName());

    static {
        SystemOutConsoleHandler handler = new SystemOutConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleConsoleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.INFO);
    }

    private final SignUpController logic = new SignUpController();

    public void start() throws NoTutorFoundException {

        LOGGER.info("\n=== SIGN UP ===");
        String role = ask("Role (Student/Tutor):");
        String email = ask("Email:");
        String username = ask("Username:");
        String password = ask("Password:");
        String name = ask("Name:");
        String surname = ask("Surname:");
        LocalDate birthday = askDate("Birthday");

        UserBean userBean = new UserBean();
        userBean.setUsername(username);
        userBean.setEmail(email);

        AccountBean accountBean = new AccountBean();
        accountBean.setRole(role);
        accountBean.setName(name);
        accountBean.setSurname(surname);
        accountBean.setBirthday(birthday);


        if ("Student".equalsIgnoreCase(role)) {
            accountBean.setInstitute(ask("Institute:"));
        } else {
            accountBean.setEducationalTitle(ask("Educational Title:"));
            accountBean.setSubject(ask("Subject you teach:"));
            accountBean.setLocation(ask("Location:"));
            accountBean.setHourlyRate(Float.parseFloat(ask("Hourly Rate:")));
            accountBean.setFirstLessonFree("y".equalsIgnoreCase(ask("First lesson free? (y/n)")));

            LocalDate from = askDate("Available from:");
            LocalDate to = askDate("Available until:");
            List<DayOfWeek> days = new ArrayList<>();
            for (DayOfWeek d : DayOfWeek.values()) {
                if ("y".equalsIgnoreCase(ask("Available on " + d + "? (y/n)"))) {
                    days.add(d);
                }
            }
            AvailabilityBean availabilityBean = new AvailabilityBean();
            availabilityBean.setStartDate(from);
            availabilityBean.setEndDate(to);
            availabilityBean.setDays(days);

            try {
                availabilityBean.checkSyntax(); }
            catch (IllegalArgumentException ex) { return; }

            accountBean.setAvailabilityBean(availabilityBean);
        }

        accountBean.setPassword(password);
        accountBean.setConfirmPassword(password);

        try {
            userBean.checkEmailSyntax();
            userBean.checkUsernameSyntax();
            accountBean.checkBasicSyntax();
            accountBean.checkPasswordSyntax();
            if ("Student".equalsIgnoreCase(role)) {
                accountBean.checkStudentSyntax();
            } else {
                accountBean.checkTutorSyntax();
            }
        } catch (IllegalArgumentException ex) {
            LOGGER.warning(ex.getMessage());
            pressEnter();
            return;
        }
        // Aggiungo l'account al UserBean
        userBean.addAccount(accountBean);

        AuthResultBean authResultBean = logic.registerUser(userBean);
        if (authResultBean != null) {
            LOGGER.info("Sign-Up completed! Logged-in as "+ authResultBean.getUser().getUsername());
            new HomeGraphicControllerBW(authResultBean.getSessionId()).start();
        } else {
            LOGGER.warning("Account already exists.");
        }
        pressEnter();
    }
}


