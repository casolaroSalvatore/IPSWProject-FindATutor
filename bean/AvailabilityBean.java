package logic.bean;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class AvailabilityBean {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayOfWeek> days;

    public LocalDate getStartDate()           { return startDate; }
    public void setStartDate(LocalDate d)     { this.startDate = d; }

    public LocalDate getEndDate()             { return endDate; }
    public void setEndDate(LocalDate d)       { this.endDate = d; }

    public List<DayOfWeek> getDays()          { return days; }
    public void setDays(List<DayOfWeek> ds)   { this.days = ds; }

    public List<DayOfWeek> getDaysOfWeek()               { return days; }
    public void setDaysOfWeek(List<DayOfWeek> l){ this.days = l;  }

    // Controllo di forma: range coerente + almeno un giorno selezionato
    public void checkSyntax() {
        if (startDate == null || endDate == null ||
                startDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Start‑date cannot be in the past.");
        if (!endDate.isAfter(startDate))
            throw new IllegalArgumentException("End‑date must be after start‑date.");
        if (ChronoUnit.DAYS.between(startDate, endDate) > 365)
            throw new IllegalArgumentException("Availability range max 1 year.");
        if (days == null || days.isEmpty())
            throw new IllegalArgumentException("Select at least one week‑day.");
        if (days.stream().distinct().count() != days.size())
            throw new IllegalArgumentException("Duplicate week‑days not allowed.");
    }
}
