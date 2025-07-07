package logic.model.dao.filesystem;

import logic.model.dao.AccountDAO;
import logic.model.domain.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

// DAO filesystem per la gestione degli Account
public class FileSystemAccountDAO extends FileSystemDAO<String,Account> implements AccountDAO {

    private static final String FALSE = "false";

    private static FileSystemAccountDAO instance;

    // Costruttore: inizializza nella cartella "accounts"
    FileSystemAccountDAO(Path root) throws IOException { super(root,"accounts"); }

    // Restituisce l'istanza singleton
    public static synchronized FileSystemAccountDAO getInstance(Path root) throws IOException {
        if (instance == null) {
            instance = new FileSystemAccountDAO(root);
        }
        return instance;
    }

    // Restituisce l'ID dell'account
    @Override protected String getId(Account a) { return a.getAccountId(); }

    // Codifica un Account in lista di stringhe per salvataggio
    @Override protected List<String> encode(Account a) {
        List<String> out = new ArrayList<>(List.of(
                "accountId:" + a.getAccountId(),
                "email:"     + a.getEmail(),
                "role:"      + a.getRole(),
                "name:"      + nullSafe(a.getName()),
                "surname:"   + nullSafe(a.getSurname()),
                "birthday:"  + nullSafe(a.getBirthday()),
                "password:"  + nullSafe(a.getPassword())
        ));
        out.add("profilePic:" + nullSafe(a.getProfilePicturePath()));
        out.add("profileComment:" + nullSafe(a.getProfileComment()));

        if (a instanceof Student st) {
            out.add("institute:" + nullSafe(st.getInstitute()));
        } else if (a instanceof Tutor t) {
            out.addAll(List.of(
                    "educationalTitle:" + nullSafe(t.getEducationalTitle()),
                    "location:"         + nullSafe(t.getLocation()),
                    "subject:"          + nullSafe(t.getSubject()),
                    "hourlyRate:"       + t.getHourlyRate(),
                    "rating:"           + t.getRating(),
                    "availability:"     + encodeAvailability(t.getAvailability()),
                    "offersInPerson:"   + t.offersInPerson(),
                    "offersOnline:"     + t.offersOnline(),
                    "offersGroup:"      + t.offersGroup(),
                    "firstLessonFree:"  + t.isFirstLessonFree()
            ));
        }
        return out;
    }

    // Decodifica lista di stringhe in Account
    @Override protected Account decode(List<String> l) {
        Map<String,String> m = toMap(l);
        String role = m.get("role");
        if ("Student".equalsIgnoreCase(role)) {
            Student s = new Student(
                    m.get("email"),
                    m.get("name"),
                    m.get("surname"),
                    parseDate(m.get("birthday")),
                    m.get("institute"));
            fillCommon(s,m);
            return s;
        } else { // Tutor
            Tutor t = new Tutor.Builder(m.get("email"))
                    .name(m.get("name"))
                    .surname(m.get("surname"))
                    .birthday(parseDate(m.get("birthday")))
                    .educationalTitle(m.get("educationalTitle"))
                    .location(m.get("location"))
                    .availability(parseAvailability(m.get("availability")))
                    .subject(m.get("subject"))
                    .hourlyRate(Float.parseFloat(def(m.get("hourlyRate"), "0")))
                    .offersInPerson(Boolean.parseBoolean(def(m.get("offersInPerson"), FALSE)))
                    .offersOnline(Boolean.parseBoolean(def(m.get("offersOnline"), FALSE)))
                    .offersGroup(Boolean.parseBoolean(def(m.get("offersGroup"), FALSE)))
                    .firstLessonFree(Boolean.parseBoolean(def(m.get("firstLessonFree"), FALSE)))
                    .build();

            t.setRating(Float.parseFloat(def(m.get("rating"),"0")));
            fillCommon(t,m);
            return t;
        }
    }

    // Carica tutti gli account con ruolo specificato
    @Override
    public List<Account> loadAllAccountsOfType(String role) {
        List<Account> list = scan(e->e);
        list.removeIf(a->!role.equalsIgnoreCase(a.getRole()));
        return list;
    }

    // Helper per aumentare la modularità della classe

    // Riempie i campi comuni (profilo) di un account
    private void fillCommon(Account a, Map<String,String> m) {
        a.setProfilePicturePath(m.get("profilePic"));
        a.setProfileComment(m.get("profileComment"));

        String password = m.get("password");
        if (password != null && !password.isBlank()) {
            a.setPassword(password);
        }
    }

    // Restituisce stringa vuota se null
    private String nullSafe(Object o){ return o==null?"":o.toString(); }

    // Converte lista key:value in mappa
    private Map<String,String> toMap(List<String> ls){
        Map<String,String> m=new HashMap<>();
        for (String s:ls){ int i=s.indexOf(':'); if(i>0) m.put(s.substring(0,i), s.substring(i+1)); }
        return m;
    }

    // Parsa stringa in LocalDate
    private LocalDate parseDate(String s){ return (s==null||s.isBlank())?null:LocalDate.parse(s); }

    // Codifica Availability in stringa
    private String encodeAvailability(Availability a){
        if (a==null) return "";
        String days = a.getDaysOfWeek()==null?"": String.join("|", a.getDaysOfWeek().stream().map(Enum::name).toList());
        return a.getStartDate()+";"+a.getEndDate()+";"+days;
    }

    // Parsifica stringa in Availability
    private Availability parseAvailability(String s){
        if (s==null||s.isBlank()||!s.contains(";")) return null;
        String[] arr=s.split(";",3);
        LocalDate start=arr[0].isBlank()?null:LocalDate.parse(arr[0]);
        LocalDate end  =arr[1].isBlank()?null:LocalDate.parse(arr[1]);
        List<DayOfWeek> days=new ArrayList<>();
        if (arr.length==3 && !arr[2].isBlank()){
            for (String d:arr[2].split("\\|")) days.add(DayOfWeek.valueOf(d));
        }
        return new Availability(start,end,days);
    }

    // Restituisce valore di default se vuoto
    private String def(String v,String d){ return v==null||v.isBlank()?d:v; }
}

