package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import kartr.model.PersistenceLayer;
import kartr.services.*;

/*
This servlet processes the confirmation of registrations.
It checks the token parameter and extracts the accountid, uuid and the creation time using the split method
then updates the specified account status in the database

*/
@WebServlet("/public/confirm")
public class ConfirmServlet extends HttpServlet {
  static final Long expirationTime = (long) (1000 * 60 * 60 * 24);

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    String token = request.getParameter("token");

    // check if token is in valid syntax
    // <ACCOUNT_ID>:<REGISTER_TOKEN>:<TIMESTAMP>
    String[] arr = token.split(":");
    if (arr.length < 3) {
      return;
    }
    // extract the accountid, uuid and the expiration time (in timestamp format)
    int id = Integer.parseInt(arr[0]);
    String registerToken = arr[1];
    String timestampString = arr[2];

    // stop, if the specified token has already expired.
    if ((System.currentTimeMillis() - Long.parseLong(timestampString) > expirationTime)) {
      return;
    }

    Connection conn = DatabaseConnectionFacade.getConnection();
    /* check if the specified token exists in the database
    ... stop otherwise
    */
    String storedToken = PersistenceLayer.getTokenByAccountId(conn, id, "register_token");
    if (storedToken != null && !storedToken.equals(registerToken)) {
      DatabaseConnectionFacade.releaseConnection(conn);
      return;
    }
    // update the specified account status in the database
    if (!PersistenceLayer.updateAccountStatus(conn, id)) {
      out.println("ConfirmServlet - failed --> User " + id);
      out.println("{\"status\": \"success\", \"description\": \"account cannot be activated\"}");
    }
    DatabaseConnectionFacade.releaseConnection(conn);

    response.sendRedirect(request.getContextPath());
  }
}
