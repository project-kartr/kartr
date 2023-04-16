package kartr.model;

import java.util.ArrayList;
import java.util.List;

public class Story {
  int id;
  String headline;
  String content;
  int accountId;
  int poiId;
  String accountDisplayname;
  List<String> files;

  public Story(
      int id,
      String headline,
      String content,
      int accountId,
      int poiId,
      String authorDisplayname,
      List<String> files) {
    this.id = id;
    this.headline = headline;
    this.content = content;
    this.accountId = accountId;
    this.poiId = poiId;
    this.accountDisplayname = authorDisplayname;
    this.files = files;
  }

  public Story(
      int id, String headline, String content, int accountId, int poiId, String authorDisplayname) {
    this(id, headline, content, accountId, poiId, authorDisplayname, new ArrayList<String>());
  }

  public int getId() {
    return id;
  }

  public String getHeadline() {
    return headline;
  }

  public String getContent() {
    return content;
  }

  public int getAccountId() {
    return accountId;
  }

  public int getPoiId() {
    return poiId;
  }

  public String getAccountDisplayname() {
    return accountDisplayname;
  }

  public List<String> getFiles() {
    return files == null ? new ArrayList<String>() : files;
  }
}
