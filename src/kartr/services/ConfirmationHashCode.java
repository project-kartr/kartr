package kartr.services;

import java.sql.*;

public class ConfirmationHashCode {
  public static String genMD5(Connection conn, String email) {
    String result = "";
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "SELECT MD5(password) from account WHERE email = ? "); // TODO: add AND is_activ =
      // FALSE
      ps.setString(1, email);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) result = rs.getString(1);
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // DatabaseConnectionFacade.releaseConnection(conn);
    return result;
  }
}
