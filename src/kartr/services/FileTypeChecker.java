package kartr.services;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/*
 * Class to check files (from InputStream) for allowed file types with
 * ImageMagick's identify.
 */

public class FileTypeChecker {
  public static final String[] allowedFileTypes = {"PNG", "JPEG"};

  public static String getMimeType(InputStream input) {
    try {
      String format = identifyFile(input);
      String mimeType = null;

      switch (format) {
        case "PNG":
          mimeType = "image/png";
          break;
        case "JPEG":
          mimeType = "image/jpeg";
          break;
      }

      return mimeType;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean checkAllowedFileTypes(InputStream input) {
    boolean allowed = false;

    try {
      String format = identifyFile(input);

      if (format != null) {
        for (String type : allowedFileTypes) {
          if (format.equals(type)) {
            allowed = true;
            break;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return allowed;
  }

  // call ImageMagick's identify to get mime-type of file
  public static String identifyFile(InputStream input) throws IOException {
    ProcessBuilder pb = new ProcessBuilder("identify", "-format", "%m", "-");
    Process proc = pb.start();

    // try-with-resources for auto-close of streams
    try (OutputStream stdin = proc.getOutputStream(); ) {
      input.transferTo(stdin);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    BufferedReader stdout = proc.inputReader();
    return stdout.readLine(); // identify should only return one line
  }
}
