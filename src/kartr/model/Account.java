package kartr.model;

public record Account(
    int id, String email, String password, String displayname, boolean isAdmin, boolean isActive) {
  public Account(String email, String password, String displayname) {
    this(0, email, password, displayname, false, false);
  }
}
