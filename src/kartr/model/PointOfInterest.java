package kartr.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PointOfInterest {
  int id;
  BigDecimal longitude;
  BigDecimal latitude;
  String displayname;
  int accountId;
  List<Integer> stories;

  public PointOfInterest(
      int id,
      BigDecimal lon,
      BigDecimal lat,
      String displayname,
      int accountId,
      List<Integer> stories) {
    this.id = id;
    this.displayname = displayname;
    this.latitude = lat;
    this.longitude = lon;
    this.accountId = accountId;
    this.stories = stories;
  }

  public PointOfInterest(
      int id, BigDecimal lon, BigDecimal lat, String displayname, int accountId) {
    this(id, lon, lat, displayname, accountId, new ArrayList<Integer>());
  }

  public int getId() {
    return id;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public String getDisplayname() {
    return displayname;
  }

  public int getAccountId() {
    return accountId;
  }

  public List<Integer> getStories() {
    return stories;
  }
}
