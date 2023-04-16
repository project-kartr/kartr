package kartr.model;

import java.sql.*;
import java.util.Objects;
import java.util.Optional;

public class UserValidation {
  public static boolean isUser(Connection conn, String email, String password) {
    boolean isValid = false;
    try {
      String query = "SELECT * FROM account WHERE email=? AND password=?";
      ResultSet rs = doSelect(conn, query, email, password);
      isValid = rs.next();
      rs.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return isValid;
  }

  public static Optional<Account> getAccountFromEmail(Connection conn, String tmpEmail) {
    int id = 0;
    String email = null;
    String password = null;
    String displayname = null;
    boolean isAdmin = false;
    boolean isActive = false;

    try {
      String query = "SELECT * FROM account WHERE email=?";
      ResultSet rs = doSelect(conn, query, tmpEmail);
      int rowsFound = 0;
      while (rs.next()) {
        ++rowsFound;
        id = rs.getInt("id");
        email = rs.getString("email");
        password = Objects.requireNonNull(rs.getString("password"), "password in db not found");
        displayname = rs.getString("displayname");
        isAdmin = rs.getBoolean("is_admin");
        isActive = rs.getBoolean("is_active");
      }

      Optional<Account> o_acc = Optional.empty();
      // cases for rowsFound:
      // 0 => fail: failed to find matching entry (ok)
      // 1 => success: found one matching entry (ok)
      // >1 => super fail: found more than one entry (NOT OK)
      if (rowsFound == 1) {
        o_acc = Optional.of(new Account(id, email, password, displayname, isAdmin, isActive));
      } else if (rowsFound > 1) {
        // This should not happen, as the email column should be unique in the DB.
        System.err.println(
            "THIS SHOULD NOT HAPPEN (Check your database setup): "
                + "The database returned multiple entries matching an email-address."
                + "Did you forget to set the database column to be unique?");
      }
      rs.close();
      return o_acc;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  private static ResultSet doSelect(Connection conn, String query, String... args) {
    PreparedStatement ps;
    ResultSet rs;
    try {
      ps = conn.prepareStatement(query);
      for (int i = 0; i < args.length; i++) {
        ps.setString(i + 1, args[i]);
      }
      rs = ps.executeQuery();
      return rs;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
