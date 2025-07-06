package logic.exception.dao;

import java.nio.file.Path;

public class AccountDAOInitException extends RuntimeException {
    public AccountDAOInitException(Path base, Throwable cause) {
        super("Cannot create FileSystemAccountDAO at " + base, cause);
    }
}
