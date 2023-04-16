package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;

/*
 * This Servlet is accessible under the URL auth/login, so it is not filtered by the AuthenticationFilter.
 * The LoginStatus Servlet is requested when it is necessary to check whether the user is logged in or not.
 * If the user has a valid session, the displayname will be returned.
 */

@WebServlet("auth/status")
public class LoginStatus extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("application/json"); // json type
    PrintWriter out = response.getWriter();
    HttpSession session = request.getSession(false);
    if (session != null) {
      String displayName = (String) session.getAttribute("displayName");
      if (displayName != null) {
        out.println("{\"status\": \"success\", \"displayName\": \"" + displayName + "\"}");
      } else {
        session.invalidate();
        out.println("{\"status\": \"failed\" }");
      }
    } else {
      out.println("{\"status\": \"failed\" }");
    }
  }
}
