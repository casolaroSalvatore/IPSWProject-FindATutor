package logic.exception.filesystem;

import java.nio.file.Path;

// Eccezione per problemi di scansione directory
public class DirectoryScanException extends RuntimeException {
  public DirectoryScanException(Path dir, Throwable cause) {
    super("Unable to scan directory: " + dir, cause);
  }
}