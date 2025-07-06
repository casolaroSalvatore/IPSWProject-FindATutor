package logic.bean;

public class TutorBean extends AccountBean {

    private String subject;
    private int age;
    private float hourlyRate;
    private float rating;
    private boolean offersInPerson;
    private boolean offersOnline;
    private boolean offersGroup;
    private boolean firstLessonFree;

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public void setSubject(String s) {
        subject = s;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public float getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public void setHourlyRate(float h) {
        hourlyRate = h;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float r) {
        rating = r;
    }

    @Override
    public boolean isOffersInPerson() {
        return offersInPerson;
    }

    @Override
    public void setOffersInPerson(boolean v) {
        offersInPerson = v;
    }

    @Override
    public boolean isOffersOnline() {
        return offersOnline;
    }

    @Override
    public void setOffersOnline(boolean v) {
        offersOnline = v;
    }

    @Override
    public boolean isOffersGroup() {
        return offersGroup;
    }

    @Override
    public void setOffersGroup(boolean v) {
        offersGroup = v;
    }

    @Override
    public boolean isFirstLessonFree() {
        return firstLessonFree;
    }

    @Override
    public void setFirstLessonFree(boolean v) {
        firstLessonFree = v;
    }
}
