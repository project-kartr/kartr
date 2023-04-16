package kartr.frontend;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import kartr.model.PersistenceLayer;
import kartr.services.DatabaseConnectionFacade;
import kartr.services.FileIdMatcher;
import kartr.services.FileStorageFacade;
import kartr.services.ResponseHelper;

@WebServlet("/public/get-file-by-id")
public class GetFileByIdServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // be careful where we write stuff to the client: check whether the
    // responseHelper sends a response (here only errors) or this Servlet (with
    // fs.readFile)
    ResponseHelper responseHelper = new ResponseHelper(response);

    try {
      String fileId = request.getParameter("file_id");
      FileIdMatcher fidm = new FileIdMatcher();
      if (fileId != null) {
        if (fidm.matches(fileId)) {
          Connection conn = DatabaseConnectionFacade.getConnection();
          String mimeType = PersistenceLayer.getMimeTypeByFileId(conn, fileId);
          DatabaseConnectionFacade.releaseConnection(conn);
          if (mimeType != null) {
            response.setContentType(mimeType);
            FileStorageFacade fs = new FileStorageFacade();
            OutputStream out = response.getOutputStream();
            fs.readFile(fileId, out);
          } else {
            responseHelper.createError("the requested file has an unknown type", true);
          }
        } else {
          responseHelper.createError("file not found", true);
        }
      } else {
        responseHelper.createError("file_id not provided", true);
      }
    } catch (IOException e) {
      e.printStackTrace();
      responseHelper.createError("we could not read the file", true);
    }

    if (responseHelper.hasErrors()) {
      response.reset();
      responseHelper.sendErrors();
    }
  }
}
