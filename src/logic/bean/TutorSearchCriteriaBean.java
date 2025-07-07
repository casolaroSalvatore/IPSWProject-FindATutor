package logic.bean;

import java.io.Serializable;

// TutorSearchCriteriaBean è un Bean che trasporta i criteri di ricerca dei tutor
// tra View e Controller. Include filtri come materia, località, disponibilità e opzioni.
public class TutorSearchCriteriaBean implements Serializable {
    private String subject;
    private String location;
    private AvailabilityBean availability;

    private boolean inPerson;
    private boolean online;
    private boolean group;
    private boolean rating4Plus;
    private boolean firstLessonFree;
    private String orderCriteria;

    // Utilizzo un Builder per annullare l'errore di SonarQube "Constructor has 9 parameters, which is greater than 7 authorized."
    public static class Builder {
        private final TutorSearchCriteriaBean bean = new TutorSearchCriteriaBean();

        public Builder subject(String subject) {
            bean.subject = subject;
            return this;
        }

        public Builder location(String location) {
            bean.location = location;
            return this;
        }

        public Builder availability(AvailabilityBean availability) {
            bean.availability = availability;
            return this;
        }

        public Builder inPerson(boolean inPerson) {
            bean.inPerson = inPerson;
            return this;
        }

        public Builder online(boolean online) {
            bean.online = online;
            return this;
        }

        public Builder group(boolean group) {
            bean.group = group;
            return this;
        }

        public Builder rating4Plus(boolean rating4Plus) {
            bean.rating4Plus = rating4Plus;
            return this;
        }

        public Builder firstLessonFree(boolean firstLessonFree) {
            bean.firstLessonFree = firstLessonFree;
            return this;
        }

        public Builder orderCriteria(String orderCriteria) {
            bean.orderCriteria = orderCriteria;
            return this;
        }

        public TutorSearchCriteriaBean build() {
            return bean;
        }
    }

    /* Getter e Setter standard */

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public AvailabilityBean getAvailability() { return availability; }
    public void setAvailability(AvailabilityBean availability) { this.availability = availability; }

    public boolean isInPerson() { return inPerson; }
    public void setInPerson(boolean inPerson) { this.inPerson = inPerson; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public boolean isGroup() { return group; }
    public void setGroup(boolean group) { this.group = group; }

    public boolean isRating4Plus() { return rating4Plus; }
    public void setRating4Plus(boolean rating4Plus) { this.rating4Plus = rating4Plus; }

    public boolean isFirstLessonFree() { return firstLessonFree; }
    public void setFirstLessonFree(boolean firstLessonFree) { this.firstLessonFree = firstLessonFree; }

    public String getOrderCriteria() { return orderCriteria; }
    public void setOrderCriteria(String orderCriteria) { this.orderCriteria = orderCriteria; }

}

