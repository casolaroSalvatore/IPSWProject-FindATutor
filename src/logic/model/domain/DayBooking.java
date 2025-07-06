package logic.model.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class DayBooking {

    private LocalDate date;
    private String startTime;
    private String endTime;
    private String comment;
    private boolean selected;

    public DayBooking(LocalDate date) {
        this.date = date;
        this.startTime = "";
        this.endTime = "";
        this.comment = "";
        this.selected = false;
    }

    public LocalDate getDate()             { return date; }
    public void      setDate(LocalDate d)  { this.date = d; }

    public String getStartTime()           { return startTime; }
    public void   setStartTime(String s)   { this.startTime = s; }

    public String getEndTime()             { return endTime; }
    public void   setEndTime(String e)     { this.endTime = e; }

    public String getComment()             { return comment; }
    public void   setComment(String c)     { this.comment = c; }

    public boolean isSelected()            { return selected; }
    public void   setSelected(boolean sel) { this.selected = sel; }

    public LocalTime getStartTimeParsed() {
        return (startTime == null || startTime.isBlank()) ? null : LocalTime.parse(startTime);
    }
    public LocalTime getEndTimeParsed()   {
        return (endTime   == null || endTime  .isBlank()) ? null : LocalTime.parse(endTime);
    }
    public boolean   missingTimes() {
        return startTime == null || startTime.isBlank()
                || endTime   == null || endTime  .isBlank();
    }
}

