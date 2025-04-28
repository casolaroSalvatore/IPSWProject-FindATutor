package logic.exception.filesystem;

import java.nio.file.Path;

public class AccountDaoInitException extends RuntimeException {
    public AccountDaoInitException(Path base, Throwable cause) {
        super("Cannot create FileSystemAccountDAO at " + base, cause);
    }
}
