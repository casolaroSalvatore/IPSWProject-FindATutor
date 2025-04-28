package logic.exception.filesystem;

import java.nio.file.Path;

public class TutoringSessionDaoInitException   extends RuntimeException {
    public TutoringSessionDaoInitException(Path base, Throwable cause) {
        super("Cannot create FileSystemTutoringSessionDAO at " + base, cause);
    }
}
