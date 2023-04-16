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
  Overall, this servlet is responsible for managing story deletions and ensuring that users can only delete stories they are authorized to delete.
  If the story is successfully deleted, the program sends a message back to the user indicating success.
  If there was an error deleting the story or the user is not authorized, the program sends a message indicating failure.
*/
@WebServlet("/api/story-delete")
@MultipartConfig
public class StoryDeleteServlet extends HttpServlet {

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
        int storyId = Integer.parseInt(request.getParameter("story_id"));
        if (storyId > 0) {
          if (account.isAdmin() || PersistenceLayer.getStoryAuthorId(conn, storyId) == accountId) {
            if (!PersistenceLayer.deleteStory(conn, storyId)) {
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
      ++errors;
    }

    DatabaseConnectionFacade.releaseConnection(conn);

    String jsonResponse = "{ \"status\": \"" + (errors == 0 ? "success" : "failed") + "\" }";
    out.println(jsonResponse);
  }
}
