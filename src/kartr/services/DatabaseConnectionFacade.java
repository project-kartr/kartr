package kartr.services;

import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import javax.naming.*;
import javax.sql.*;

public class DatabaseConnectionFacade {
  public static Connection getConnection() {
    try {
      Properties properties = new Properties();
      properties.load(DatabaseConnectionFacade.class.getResourceAsStream("connection.properties"));
      String datasource = properties.getProperty("datasource");

      Context initCtx = new InitialContext();
      DataSource ds = (DataSource) initCtx.lookup(datasource);
      Connection conn = ds.getConnection();
      return conn;
    } catch (SQLException | NamingException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void releaseConnection(Connection conn) {
    try {
      conn.close();
    } catch (SQLException sqle) {
      throw new RuntimeException(sqle);
    }
  }
}
