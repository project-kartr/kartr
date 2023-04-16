package kartr.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/*
 * Facade to handle File Storage actions like writing, reading and getting the
 * mime-type. Currently using the OS FileSystem, but should be possible to
 * change to any other storage backend (like S3).
 */

public class FileStorageFacade {
  private static String UPLOAD_BASE_DIR = "/opt/bub-data/";
  private static boolean initialized = false;

  // Don't use filenames provided by users.
  // Not using jakarta.servlet.http.Part to reduce needed dependencies in backend
  public void writeFile(String fileName, InputStream data) throws IOException {
    Files.copy(data, Paths.get(UPLOAD_BASE_DIR + fileName), StandardCopyOption.REPLACE_EXISTING);
  }

  public void readFile(String fileId, OutputStream out) throws IOException {
    InputStream fileInput = Files.newInputStream(Paths.get(UPLOAD_BASE_DIR + fileId));
    byte[] buffer = new byte[1024];
    while (fileInput.read(buffer) != -1) {
      out.write(buffer);
    }
  }

  public InputStream getFileInputStream(String fileId) throws IOException {
    return Files.newInputStream(Paths.get(UPLOAD_BASE_DIR + fileId));
  }

  public String getMimeType(String fileId) {
    try (InputStream input = getFileInputStream(fileId)) {
      return FileTypeChecker.getMimeType(input);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public boolean deleteFile(String fileName) throws IOException {
    return Files.deleteIfExists(Paths.get(UPLOAD_BASE_DIR + fileName));
  }

  public FileStorageFacade() {
    if (!initialized) {
      init();
      initialized = true;
    }
  }

  private synchronized void init() {
    try {
      // get and create upload base dir from properties file and
      // append '/' if needed
      Properties properties = new Properties();
      properties.load(DatabaseConnectionFacade.class.getResourceAsStream("filestorage.properties"));
      UPLOAD_BASE_DIR = properties.getProperty("upload_base_dir");
      if (!UPLOAD_BASE_DIR.endsWith("/")) {
        UPLOAD_BASE_DIR += "/";
      }
      File uploadPath = new File(UPLOAD_BASE_DIR);
      if (!uploadPath.exists()) {
        uploadPath.mkdir();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
