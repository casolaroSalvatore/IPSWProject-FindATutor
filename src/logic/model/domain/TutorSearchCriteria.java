package logic.model.domain;

import logic.bean.AvailabilityBean;

// Classe ausiliaria creata unicamente per silenziare l'errore su SonarQube
public record TutorSearchCriteria(
        String subject,
        String location,
        AvailabilityBean userAvailability,
        boolean inPerson,
        boolean online,
        boolean group,
        boolean rating4Plus,
        boolean firstLessonFree,
        String orderCriteria
) {}
