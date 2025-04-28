package logic.model.domain;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;

public class DayBooking {

    private final ObjectProperty<LocalDate> date;
    private final StringProperty startTime;
    private final StringProperty endTime;
    private final StringProperty comment;
    private final BooleanProperty selected;

    public DayBooking(LocalDate date) {
        this.date = new SimpleObjectProperty<>(date);
        this.startTime = new SimpleStringProperty("");
        this.endTime = new SimpleStringProperty("");
        this.comment = new SimpleStringProperty("");
        this.selected = new SimpleBooleanProperty(false);
    }

    // Getters (per la tabella) e property
    public LocalDate getDate() {
        return date.get();
    }
    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public String getStartTime() {
        return startTime.get();
    }
    public StringProperty startTimeProperty() {
        return startTime;
    }
    public void setStartTime(String value) {
        this.startTime.set(value);
    }

    public String getEndTime() {
        return endTime.get();
    }
    public StringProperty endTimeProperty() {
        return endTime;
    }
    public void setEndTime(String value) {
        this.endTime.set(value);
    }

    public String getComment() {
        return comment.get();
    }
    public StringProperty commentProperty() {
        return comment;
    }
    public void setComment(String value) {
        this.comment.set(value);
    }

    public boolean isSelected() {
        return selected.get();
    }
    public BooleanProperty selectedProperty() {
        return selected;
    }
    public void setSelected(boolean value) {
        this.selected.set(value);
    }

    // restituisce lo startTime già convertito in LocalTime
    public LocalTime getStartTimeParsed() {
        return (startTime.get() == null || startTime.get().isBlank())
                ? null : LocalTime.parse(startTime.get());
    }

    // restituisce l’endTime già convertito in LocalTime
    public LocalTime getEndTimeParsed() {
        return (endTime.get() == null || endTime.get().isBlank())
                ? null : LocalTime.parse(endTime.get());
    }

    // comodità per capire se mancano gli orari (usato dal controller grafico)
    public boolean missingTimes() {
        return startTime.get() == null || startTime.get().isBlank()
                || endTime  .get() == null || endTime  .get().isBlank();
    }
}
