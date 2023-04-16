package kartr.model;

public class AppException extends Exception {
  boolean publicError = false;

  public AppException(String s, boolean publicError) {
    super(s);
    this.publicError = publicError;
  }

  public boolean isPublic() {
    return publicError;
  }
}
