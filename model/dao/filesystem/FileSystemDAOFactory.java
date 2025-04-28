package logic.model.dao.filesystem;

import logic.exception.filesystem.AccountDaoInitException;
import logic.exception.filesystem.SharedReviewDaoInitException;
import logic.exception.filesystem.TutoringSessionDaoInitException;
import logic.exception.filesystem.UserDaoInitException;
import logic.model.dao.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileSystemDAOFactory extends DaoFactory {

    private final Path base = Paths.get(System.getProperty("user.home"), "Desktop", "myAppFS");

    public FileSystemDAOFactory() throws IOException {
        Files.createDirectories(base);
    }

    @Override
    public UserDAO getUserDAO() {
        try {
            return new FileSystemUserDAO(base);
        } catch (IOException e) {
            throw new UserDaoInitException(base, e);
        }
    }

    @Override
    public AccountDAO getAccountDAO() {
        try {
            return new FileSystemAccountDAO(base);
        } catch (IOException e) {
            throw new AccountDaoInitException(base, e);
        }
    }

    @Override
    public TutoringSessionDAO getTutoringSessionDAO() {
        try {
            return new FileSystemTutoringSessionDAO(base);
        } catch (IOException e) {
            throw new TutoringSessionDaoInitException(base, e);
        }
    }

    @Override
    public SharedReviewDAO getSharedReviewDAO() {
        try {
            return new FileSystemSharedReviewDAO(base);
        } catch (IOException e) {
            throw new SharedReviewDaoInitException(base, e);
        }
    }
}
