package logic.bean;

public class TutorBean extends AccountBean {

    private String  subject;
    private int age;
    private float   hourlyRate;
    private float   rating;
    private boolean offersInPerson, offersOnline, offersGroup, firstLessonFree;

    public String  getSubject()                 { return subject; }
    public void    setSubject(String s)         { subject = s;    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float   getHourlyRate()              { return hourlyRate; }
    public void    setHourlyRate(float h)       { hourlyRate = h; }

    public float   getRating()                  { return rating; }
    public void    setRating(float r)           { rating = r; }

    public boolean isOffersInPerson()           { return offersInPerson; }
    public void    setOffersInPerson(boolean v) { offersInPerson = v; }

    public boolean isOffersOnline()             { return offersOnline; }
    public void    setOffersOnline(boolean v)   { offersOnline = v; }

    public boolean isOffersGroup()              { return offersGroup; }
    public void    setOffersGroup(boolean v)    { offersGroup = v; }

    public boolean isFirstLessonFree()          { return firstLessonFree; }
    public void    setFirstLessonFree(boolean v){ firstLessonFree = v; }
}
