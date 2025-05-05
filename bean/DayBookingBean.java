package logic.bean;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

public class DayBookingBean {

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<String> startTime = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<String> endTime = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<String> comment = new SimpleObjectProperty<>();

    public DayBookingBean(LocalDate d) { date.set(d); }

    public BooleanProperty selectedProperty() { return selected; }
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean v) { selected.set(v); }

    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate d) { date.set(d); }

    public String getStartTime() { return startTime.get(); }
    public void setStartTime(String t) { startTime.set(t); }
    public String getEndTime() { return endTime.get(); }
    public void setEndTime(String t) { endTime.set(t); }
    public String getComment() { return comment.get(); }
    public void setComment(String c) { comment.set(c); }

    public LocalTime getStartTimeParsed() {
        String v = startTime.get();          // <-- legge la stringa
        return (v == null || v.isBlank()) ? null : LocalTime.parse(v);
    }

    public LocalTime getEndTimeParsed() {
        String v = endTime.get();            // <-- legge la stringa
        return (v == null || v.isBlank()) ? null : LocalTime.parse(v);
    }

    public boolean missingTimes(){
        return getStartTime()==null || getEndTime()==null || getStartTime().isBlank() || getEndTime().isBlank();
    }

    public void checkSyntax() {
        if (date.get() == null || date.get().isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Date must be today or later.");
        if (missingTimes())
            throw new IllegalArgumentException("Start/End time required.");
        LocalTime s = getStartTimeParsed();
        LocalTime e = getEndTimeParsed();
        if (s == null || e == null || !s.isBefore(e))
            throw new IllegalArgumentException("Start must be before End.");
        if (Duration.between(s, e).toMinutes() < 60)
            throw new IllegalArgumentException("Minimum slot 1 hour.");
        if (s.isBefore(LocalTime.of(7,0)) || e.isAfter(LocalTime.of(22,0)))
            throw new IllegalArgumentException("Time must be between 07:00 and 22:00.");
    }
}

