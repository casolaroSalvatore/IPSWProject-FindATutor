package test;

import logic.bean.TutoringSessionBean;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.Assert.*;

/* Valida il metodo checkSyntax() di TutoringSessionBean in due scenari opposti */
class TutoringSessionBeanTest {

    private TutoringSessionBean valid() {
        TutoringSessionBean b = new TutoringSessionBean();
        b.setTutorId("tutor_1");
        b.setStudentId("student_1");
        b.setDate(LocalDate.now().plusDays(2));
        b.setStartTime(LocalTime.of(14, 0));
        b.setEndTime(LocalTime.of(15, 0));
        b.setLocation("Online");
        b.setSubject("Math");
        return b;
    }

    @Test
    void testCheckSyntaxValid() {
        boolean ok = true;
        try { valid().checkSyntax(); } catch (IllegalArgumentException e) { ok = false; }
        assertEquals(true, ok);
    }

    @Test
    void testCheckSyntaxNonValid() {
        TutoringSessionBean b = valid();
        b.setEndTime(null);
        boolean failed = false;
        try { b.checkSyntax(); } catch (IllegalArgumentException e) { failed = true; }
        assertEquals(true, failed);
    }
}

