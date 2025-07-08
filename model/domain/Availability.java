package logic.model.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class Availability {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<DayOfWeek> daysOfWeek;

    public Availability(LocalDate start, LocalDate end, List<DayOfWeek> days) {
        this.startDate = start;
        this.endDate = end;
        this.daysOfWeek = days;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<DayOfWeek> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

}

