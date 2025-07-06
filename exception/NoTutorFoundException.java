package logic.exception;

/* La ricerca da parte dello Student non produce risultati */
public class NoTutorFoundException extends Exception {
  public NoTutorFoundException(String details) { super(details); }
}
