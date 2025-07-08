package logic.model.dao.filesystem;

import logic.exception.dao.AccountDAOInitException;
import logic.exception.dao.SharedReviewDAOInitException;
import logic.exception.dao.TutoringSessionDAOInitException;
import logic.exception.filesystem.UserDaoInitException;
import logic.infrastructure.DaoFactory;
import logic.model.dao.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// Factory che fornisce i DAO filesystem
public class FileSystemDAOFactory extends DaoFactory {

    private final Path base = Paths.get(System.getProperty("user.home"), "Desktop", "myAppFS");

    public FileSystemDAOFactory() throws IOException {
        Files.createDirectories(base);
    }

    // Restituisce il DAO per gli utenti
    @Override
    public UserDAO getUserDAO() {
        try {
            return new FileSystemUserDAO(base);
        } catch (IOException e) {
            throw new UserDaoInitException(base, e);
        }
    }

    // Restituisce il DAO per gli account
    @Override
    public AccountDAO getAccountDAO() {
        try {
            return new FileSystemAccountDAO(base);
        } catch (IOException e) {
            throw new AccountDAOInitException(base, e);
        }
    }

    // Restituisce il DAO per le sessioni di tutoraggio
    @Override
    public TutoringSessionDAO getTutoringSessionDAO() {
        try {
            return new FileSystemTutoringSessionDAO(base);
        } catch (IOException e) {
            throw new TutoringSessionDAOInitException(base, e);
        }
    }

    // Restituisce il DAO per le recensioni condivise
    @Override
    public SharedReviewDAO getSharedReviewDAO() {
        try {
            return new FileSystemSharedReviewDAO(base);
        } catch (IOException e) {
            throw new SharedReviewDAOInitException(base, e);
        }
    }
}
