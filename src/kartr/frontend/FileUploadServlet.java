package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.UUID;
import kartr.services.FileStorageFacade;
import kartr.services.FileTypeChecker;
import kartr.services.ResponseHelper;

@WebServlet("/api/file-upload")
@MultipartConfig
public class FileUploadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    // Upload Ablauf
    // 1. frontend: Bilder auswählen und im Hintergrund hochladen
    //   - XHR: Upload eines Bildes pro Request
    //     - Return value: Random Name von Bild auf Server.
    //   - JS: XHR Return value Bildname merken
    // 2. frontend: story schreiben
    // 3. frontend: story abschicken
    //   - XHR: Upload Story Inhalte + Bildernamen aus den Serverantworten
    // 4. backend: persist story in DB and create story-file relations

    ResponseHelper responseHelper = new ResponseHelper(response);

    response.setContentType("application/json");
    String fileId = System.currentTimeMillis() + "_" + UUID.randomUUID().toString();

    try {
      FileStorageFacade fs = new FileStorageFacade();
      Part p = request.getPart("thefile");
      String contentType = p.getContentType();
      // TODO: put allowed mime-types in a list, if we want to allow more types
      if (contentType.equals("image/png") || contentType.equals("image/jpeg")) {
        // TODO: maybe handle IOException and send error description to client
        fs.writeFile(fileId, p.getInputStream());

        boolean allowed = FileTypeChecker.checkAllowedFileTypes(fs.getFileInputStream(fileId));
        if (!allowed) {
          responseHelper.createError("the file provided did not comply with our policies", true);
          fs.deleteFile(fileId);
        }
      } else {
        responseHelper.createError(
            "wrong content-type. allowed are image/png and image/jpeg", true);
      }
    } catch (IOException e) {
      e.printStackTrace();
      responseHelper.createError("we have some I/O issues", true);
    }

    responseHelper.sendResponse("file_id", fileId);
  }
}
