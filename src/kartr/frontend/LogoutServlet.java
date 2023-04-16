package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import kartr.model.*;
import kartr.services.*;

/* This servlet is accessible under the URL api/logout and so is filtered by the AuthenticationFilter.
 * The LogoutServlet invalidates an existing session.
 */

@WebServlet("/api/logout")
@MultipartConfig
public class LogoutServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();

    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
      out.println("{\"status\": \"success\" }");
    } else {
      out.println("{\"status\": \"failed\" }");
    }
  }
}
