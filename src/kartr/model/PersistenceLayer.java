package kartr.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class PersistenceLayer {
  public static List<PointOfInterest> findAllPointOfInterest(Connection conn) {
    List<PointOfInterest> pois = new ArrayList<>();
    try {
      PreparedStatement ps = conn.prepareStatement("select * from poi");
      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        int poiId = rs.getInt("id");

        List<Integer> stories = new ArrayList<Integer>();
        // NOTE: for increasing amount of data or requests, we might want to
        // change this to query only once by using a table join:
        // `select * from poi left outer join story on poi.id = story.poi_id`
        PreparedStatement storyQuery =
            conn.prepareStatement("select id from story where poi_id = ?");
        storyQuery.setInt(1, poiId);
        ResultSet storyRs = storyQuery.executeQuery();

        while (storyRs.next()) {
          stories.add(storyRs.getInt("id"));
        }

        PointOfInterest poi =
            new PointOfInterest(
                rs.getInt("id"),
                rs.getBigDecimal("longitude"),
                rs.getBigDecimal("latitude"),
                rs.getString("displayname"),
                rs.getInt("account_id"),
                stories);
        pois.add(poi);
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
    }
    return pois;
  }

  public static Story getStoryById(Connection conn, int id) {
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "select *,"
                  + " (select displayname from account where id = story.account_id)"
                  + " as account_displayname from story where story.id = ?;");
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();

      // should only return one item, because id unique
      rs.next();
      Story story =
          new Story(
              rs.getInt("id"),
              rs.getString("headline"),
              rs.getString("content"),
              rs.getInt("account_id"),
              rs.getInt("poi_id"),
              rs.getString("account_displayname"));

      rs.close();
      ps.close();

      List<String> files = story.getFiles();
      ps = conn.prepareStatement("select * from file where story_id = ?;");
      ps.setInt(1, id);
      rs = ps.executeQuery();

      while (rs.next()) {
        files.add(rs.getString("filename"));
      }

      return story;
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean insertFile(Connection conn, String filename, int storyId, String mimeType) {
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "insert into file (filename, story_id, mime_type) values (?, ?, ?)");
      ps.setString(1, filename);
      ps.setInt(2, storyId);
      ps.setString(3, mimeType);
      ps.execute();
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static boolean clearFilesForStory(Connection conn, int storyId) {
    try {
      PreparedStatement ps = conn.prepareStatement("delete from file where story_id = ?");
      ps.setInt(1, storyId);
      ps.execute();
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static String getMimeTypeByFileId(Connection conn, String filename) {
    try {
      PreparedStatement ps = conn.prepareStatement("select mime_type from file where filename = ?");
      ps.setString(1, filename);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString("mime_type");
      } else {
        return null;
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return null;
    }
  }

  public static boolean updateStory(
      Connection conn, int storyId, String headline, String content, int accountId, int poiId) {
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "update story set headline = ?, content = ?, account_id = ?, poi_id = ? where id = ?",
              Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, headline);
      ps.setString(2, content);
      ps.setInt(3, accountId);
      ps.setInt(4, poiId);
      ps.setInt(5, storyId);
      ps.execute();

      // this function should only update one row and therefore only generate one key
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static int getStoryIdByFilename(Connection conn, String filename) {
    try {
      PreparedStatement ps = conn.prepareStatement("select story_id from file where filename = ?");
      ps.setString(1, filename);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt("story_id");
      } else {
        return 0;
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return 0;
    }
  }

  public static int addStory(
      Connection conn, String headline, String content, int accountId, int poiId) {
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "insert into story (headline, content, account_id, poi_id) values (?, ?, ?, ?)",
              Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, headline);
      ps.setString(2, content);
      ps.setInt(3, accountId);
      ps.setInt(4, poiId);
      ps.execute();

      // this function should only insert one row and therefore only generate one key
      ResultSet keys = ps.getGeneratedKeys();
      keys.next();
      return keys.getInt(1);
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return 0;
    }
  }

  public static boolean updatePoi(
      Connection conn,
      int poiId,
      BigDecimal lon,
      BigDecimal lat,
      int accountId,
      String displayname) {
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "update poi set longitude = ?, latitude = ?, account_id = ?, displayname = ? where id"
                  + " = ?");
      ps.setBigDecimal(1, lon);
      ps.setBigDecimal(2, lat);
      ps.setInt(3, accountId);
      ps.setString(4, displayname);
      ps.setInt(5, poiId);
      ps.execute();

      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static int addPoi(
      Connection conn, BigDecimal lon, BigDecimal lat, int accountId, String displayname) {
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "insert into poi (longitude, latitude, account_id, displayname) values (?, ?, ?, ?)",
              Statement.RETURN_GENERATED_KEYS);
      ps.setBigDecimal(1, lon);
      ps.setBigDecimal(2, lat);
      ps.setInt(3, accountId);
      ps.setString(4, displayname);
      ps.execute();

      // this function should only insert one row and therefore only generate one key
      ResultSet keys = ps.getGeneratedKeys();
      keys.next();
      return keys.getInt(1);
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return 0;
    }
  }

  public static int getStoryAuthorId(Connection conn, int storyId) {
    try {
      PreparedStatement ps = conn.prepareStatement("select account_id from story where id = ?");
      ps.setInt(1, storyId);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt("account_id");
      } else {
        return 0;
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return 0;
    }
  }

  public static boolean deleteStory(Connection conn, int storyId) {
    try {
      PreparedStatement ps = conn.prepareStatement("delete from file where story_id = ?");
      ps.setInt(1, storyId);
      ps.executeUpdate();
      ps = conn.prepareStatement("delete from story where id = ?");
      ps.setInt(1, storyId);
      ps.executeUpdate();
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static boolean deleteFile(Connection conn, String filename) {
    try {
      PreparedStatement ps = conn.prepareStatement("delete from file where filename = ?");
      ps.setString(1, filename);
      ps.executeUpdate();
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static boolean deletePoi(Connection conn, int poiId) {
    try {
      // Cascade? Was ist mit den dazugehörigen Stories etc.
      PreparedStatement ps = conn.prepareStatement("delete from poi where id = ?");
      ps.setInt(1, poiId);
      ps.executeUpdate();
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static boolean deleteToken(Connection conn, int accountId, String tokenType) {
    try {
      String sql = "UPDATE account SET " + tokenType + " = ? WHERE id = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, "");
      ps.setInt(2, accountId);
      ps.executeUpdate();
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }

  public static String insertAccount(Connection conn, Account acc) {
    try {
      String sql = "INSERT INTO account (email, password, displayname, is_admin) VALUES (?,?,?,?)";
      PreparedStatement ps = conn.prepareStatement(sql, new String[] {"id", "register_token"});
      ps.setString(1, acc.email());
      ps.setString(2, acc.password());
      ps.setString(3, acc.displayname());
      ps.setBoolean(4, acc.isAdmin());
      ps.execute();
      // this function should only insert one row and therefore only generate one key
      ResultSet keys = ps.getGeneratedKeys();
      keys.next();
      // TOKEN: account_id:register_token:timestamp
      String registerToken =
          keys.getString("id")
              + ":"
              + keys.getString("register_token")
              + ":"
              + System.currentTimeMillis();
      return registerToken;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return null;
    }
  }

  public static String getTokenByAccountId(Connection conn, int id, String tokenType) {
    try {
      String sql = "SELECT " + tokenType + " FROM account where id = ?";
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, id);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getString(tokenType);
      } else {
        return "false";
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return null;
    }
  }

  public static boolean updateAccountStatus(Connection conn, int id) {
    try {
      PreparedStatement ps =
          conn.prepareStatement(
              "UPDATE account SET is_active = true WHERE is_active = false AND id = ? ");
      ps.setInt(1, id);
      ps.executeUpdate();
      ps.close();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static boolean updatePasswordResetToken(
      Connection conn, String resetPasswordToken, int accountId) {
    try {
      PreparedStatement ps =
          conn.prepareStatement("UPDATE account SET reset_password_token = ? WHERE id = ? ");
      ps.setString(1, resetPasswordToken);
      ps.setInt(2, accountId);
      ps.executeUpdate();
      ps.close();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static int getAccountIdByEmail(Connection conn, String email) {
    try {
      PreparedStatement ps = conn.prepareStatement("SELECT id from account where email = ?");
      ps.setString(1, email);
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getInt("id");
      } else {
        return 0;
      }
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return 0;
    }
  }

  public static boolean updatePassword(Connection conn, String password, int accountId) {
    try {
      PreparedStatement ps = conn.prepareStatement("UPDATE account SET password = ? WHERE id = ?");
      ps.setString(1, password);
      ps.setInt(2, accountId);
      ps.executeUpdate();
      ps.close();
      return true;
    } catch (SQLException sqle) {
      sqle.printStackTrace();
      return false;
    }
  }
}
