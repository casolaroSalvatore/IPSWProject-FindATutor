package logic.exception;

// Eccezione lanciata quando la ricerca dello studente non trova tutor
public class NoTutorFoundException extends Exception {
  public NoTutorFoundException(String details) { super(details); }
}
