package logic.bean;

import java.time.DayOfWeek;
import java.time.LocalDate;
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

    // Controllo di forma: range coerente + almeno un giorno selezionato
    public void checkSyntax() {
        if (startDate == null || endDate == null || !endDate.isAfter(startDate))
            throw new IllegalArgumentException("End-date must be after start-date.");
        if (days == null || days.isEmpty())
            throw new IllegalArgumentException("Select at least one week-day.");
    }
}
