package logic.model.dao.filesystem;

import logic.model.dao.AccountDAO;
import logic.infrastructure.DaoFactory;
import logic.model.dao.UserDAO;
import logic.model.domain.Account;
import logic.model.domain.User;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

// DAO filesystem per la gestione degli User
public class FileSystemUserDAO extends FileSystemDAO<String,User> implements UserDAO {

    private static FileSystemUserDAO instance;

    // Costruttore: inizializza nella cartella "users"
    FileSystemUserDAO(Path root) throws IOException { super(root,"users"); }

    // Restituisce l'istanza singleton
    public static synchronized FileSystemUserDAO getInstance(Path root) throws IOException {
        if (instance == null) {
            instance = new FileSystemUserDAO(root);
        }
        return instance;
    }

    private final AccountDAO accountDAO = DaoFactory.getInstance().getAccountDAO();

    // Restituisce la chiave dell'utente (email)
    @Override protected String getId(User u) { return u.getEmail(); }

    // Serializza un User in righe di file
    @Override protected List<String> encode(User u) {
        String accounts = String.join("|", u.getAccounts().stream().map(a->a.getAccountId()).toList());
        return List.of(
                "email:"     + u.getEmail(),
                "username:"  + (u.getUsername()==null?"":u.getUsername()),
                "accounts:"  + accounts
        );

    }

    // Deserializza righe di file in User
    @Override
    protected User decode(List<String> lines) {
        Map<String,String> m = toMap(lines);

        User user = new User(m.get("email"),
                m.get("username"));

        /* Carico  gli Account reali --- */
        String accLine = m.get("accounts");
        if (accLine != null && !accLine.isBlank()) {
            List<Account> list = new ArrayList<>();
            for (String id : accLine.split("\\|")) {
                Account acc = accountDAO.load(id);
                if (acc != null) list.add(acc);
            }
            user.setAccounts(list);
        }
        return user;
    }

    // Converte lista key:value in mappa
    private Map<String,String> toMap(List<String> ls){
        Map<String,String> m=new HashMap<>();
        for (String s:ls){
            int i=s.indexOf(':'); if(i > 0) m.put(s.substring(0,i), s.substring(i+1));
        }
        return m;
    }
}

