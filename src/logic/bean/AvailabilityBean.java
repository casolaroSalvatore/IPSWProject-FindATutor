package logic.bean;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

// AvailabilityBean è un Bean che trasporta disponibilità tutor
// (startDate, endDate, days) con controlli di forma.
public class AvailabilityBean implements Serializable {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayOfWeek> days;

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate d) {
        this.startDate = d;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate d) {
        this.endDate = d;
    }

    public List<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(List<DayOfWeek> ds) {
        this.days = ds;
    }

    // Controllo solo sintattico: verifica presenza e duplicati nei giorni
    public void checkSyntax() {
        if (days == null || days.isEmpty())
            throw new IllegalArgumentException("Select at least one week‑day.");
        if (days.stream().distinct().count() != days.size())
            throw new IllegalArgumentException("Duplicate week‑days not allowed.");
    }
}
