package kartr.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileIdMatcher {
  // example 1668102508076_78e6193d-a309-4449-91a3-846d1bcb2ed0
  public static final String fileIdRegex =
      "^[0-9]{13}_[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
  private Pattern pattern;

  public FileIdMatcher() {
    pattern = Pattern.compile(fileIdRegex);
  }

  public boolean matches(String fileId) {
    Matcher matcher = pattern.matcher(fileId);
    return matcher.matches();
  }
}
