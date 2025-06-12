package logic.bean;

import java.io.Serializable;

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

    public TutorSearchCriteriaBean() {};

    public TutorSearchCriteriaBean(String subject,
                                   String location,
                                   AvailabilityBean availability,
                                   boolean inPerson,
                                   boolean online,
                                   boolean group,
                                   boolean rating4Plus,
                                   boolean firstLessonFree,
                                   String orderCriteria) {
        this.subject = subject;
        this.location = location;
        this.availability = availability;
        this.inPerson = inPerson;
        this.online = online;
        this.group = group;
        this.rating4Plus = rating4Plus;
        this.firstLessonFree = firstLessonFree;
        this.orderCriteria = orderCriteria;
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

