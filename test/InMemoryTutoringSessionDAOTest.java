package test;

import logic.model.dao.inmemory.InMemoryTutoringSessionDAO;
import logic.model.domain.state.TutoringSession;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/* Controlla le operazioni fondamentali dell’InMemoryTutoringSessionDAO */
class InMemoryTutoringSessionDAOTest {

    private final InMemoryTutoringSessionDAO dao = InMemoryTutoringSessionDAO.getInstance();

    /* Creo una TutoringSession così da poterne testare i metodi */
    private TutoringSession sample() {
        TutoringSession s = new TutoringSession();
        s.setTutorId("tutor01");
        s.setStudentId("student99");
        s.setDate(LocalDate.now().plusDays(1));
        s.setStartTime(LocalTime.of(10, 0));
        s.setEndTime(LocalTime.of(11, 0));
        s.setSubject("Math");
        return s;
    }

    @Test
    void testStore() {
        TutoringSession s = sample();
        dao.store(s);
        assertNotNull(s.getSessionId());
    }

    @Test
    void testLoad() {
        TutoringSession s = sample();
        dao.store(s);
        assertEquals(s, dao.load(s.getSessionId()));
    }

    @Test
    void testDelete() {
        TutoringSession s = sample();
        dao.store(s);
        dao.delete(s.getSessionId());
        assertEquals(null, dao.load(s.getSessionId()));
    }
}