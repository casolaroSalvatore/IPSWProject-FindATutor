package logic.exception.filesystem;

import java.nio.file.Path;

// Eccezione lanciata se fallisce la creazione del FileSystemUserDAO
public class UserDaoInitException extends RuntimeException {
  public UserDaoInitException(Path base, Throwable cause) {
    super("Cannot create FileSystemUserDAO at " + base, cause);
  }
}
