package logic.exception.dao;

import java.nio.file.Path;

public class TutoringSessionDAOInitException extends RuntimeException {
    public TutoringSessionDAOInitException(Path base, Throwable cause) {
        super("Cannot create FileSystemTutoringSessionDAO at " + base, cause);
    }
}
