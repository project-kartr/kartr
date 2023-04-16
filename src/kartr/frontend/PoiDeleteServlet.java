package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import kartr.model.Account;
import kartr.model.PersistenceLayer;
import kartr.services.DatabaseConnectionFacade;

/*
  Overall, this servlet is responsible for managing POI deletions and ensuring that users can only delete POIs they are authorized to delete.
  If the POI is successfully deleted, the program sends a message back to the user indicating success.
  If there was an error deleting the POI or the user is not authorized, the program sends a message indicating failure.
*/
@WebServlet("/api/poi-delete")
@MultipartConfig
public class PoiDeleteServlet extends HttpServlet {

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
        int poiId = Integer.parseInt(request.getParameter("poi_id"));
        if (poiId > 0) {
          if (account.isAdmin()) {
            if (!PersistenceLayer.deletePoi(conn, poiId)) {
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
