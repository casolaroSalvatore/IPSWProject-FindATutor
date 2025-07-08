package logic.model.domain;

import logic.bean.AvailabilityBean;

// Rappresenta i criteri di ricerca che uno studente può utilizzare per filtrare i tutor disponibili.
// Viene utilizzata nel flusso di ricerca lato Controller logico per trovare tutor corrispondenti
// ai criteri specificati. La sua introduzione ha lo scopo, tra l’altro, di risolvere un problema
// di SonarQube su codice duplicato o parametri multipli.
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
