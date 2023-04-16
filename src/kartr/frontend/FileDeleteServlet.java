package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.regex.*;
import kartr.model.Account;
import kartr.model.PersistenceLayer;
import kartr.services.DatabaseConnectionFacade;
import kartr.services.FileIdMatcher;

/*
  Overall, this servlet is responsible for managing file deletions and ensuring that users can only delete files they are authorized to delete.
  If the file is successfully deleted, the program sends a message back to the user indicating success.
  If there was an error deleting the file or the user is not authorized, the program sends a message indicating failure.
*/

@WebServlet("/api/file-delete")
@MultipartConfig
public class FileDeleteServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Connection conn = DatabaseConnectionFacade.getConnection();

    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    int errors = 0;
    HttpSession session = request.getSession();

    try {
      Account account = (Account) session.getAttribute("account");
      if (account == null) {
        ++errors;
      } else {
        int accountId = account.id();
        String filename = request.getParameter("filename");
        FileIdMatcher fidm = new FileIdMatcher();
        if (fidm.matches(filename)) {
          if (account.isAdmin()
              || PersistenceLayer.getStoryAuthorId(
                      conn, PersistenceLayer.getStoryIdByFilename(conn, filename))
                  == accountId) {
            if (!PersistenceLayer.deleteFile(conn, filename)) {
              ++errors;
            }
          } else {
            ++errors;
          }
        } else {
          ++errors;
        }
      }
    } catch (ClassCastException e) {
      e.printStackTrace();
      ++errors;
    }

    DatabaseConnectionFacade.releaseConnection(conn);

    String jsonResponse = "{ \"status\": \"" + (errors == 0 ? "success" : "failed") + "\" }";
    out.println(jsonResponse);
  }
}
