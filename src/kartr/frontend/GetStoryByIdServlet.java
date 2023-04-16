package kartr.frontend;

import jakarta.json.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import kartr.model.*;
import kartr.services.DatabaseConnectionFacade;

@WebServlet("/public/get-story-by-id")
public class GetStoryByIdServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");

    String ids = request.getParameter("story_id");

    // accId stays 0 if user is not logged in
    int accId = 0;

    try {
      HttpSession session = request.getSession();
      Account acc = (Account) session.getAttribute("account");
      if (acc != null) {
        accId = acc.id();
      }
    } catch (ClassCastException e) {
      e.printStackTrace();
    }

    try {
      if (ids != null && !ids.isEmpty()) {
        int id = Integer.parseInt(ids);
        Connection conn = DatabaseConnectionFacade.getConnection();
        Story story = PersistenceLayer.getStoryById(conn, id);
        if (story != null) {
          boolean isOwner = story.getAccountId() == accId;
          JsonObject json = StoryConversion.convertToJsonWithStatus(story, "success", isOwner);
          out.println(json.toString());
        } else {
          out.println("{ \"status\": \"failed\", \"message\": \"story_id does not exist\" }");
        }
        DatabaseConnectionFacade.releaseConnection(conn);
      } else {
        out.println("{ \"status\": \"failed\" }");
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
      out.println("{ \"status\": \"failed\" }");
    }
  }
}
