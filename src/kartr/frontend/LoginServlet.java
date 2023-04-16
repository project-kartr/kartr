package kartr.frontend;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.util.Optional;
import kartr.model.*;
import kartr.services.*;

/*
 * This Servlet is accessible under the URL auth/login, so it is not filtered by the AuthenticationFilter.
 * The LoginServlet is responsible for the authentication and authorization of a user.
 * For this purpose, the parameters e-mail and password must be passed via POST.
 * Afterwards it will be checked if the user exists in the database and if the password is correct.
 */

@WebServlet("auth/login")
@MultipartConfig
public class LoginServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

    if (email != null && password != null) {
      Connection conn = DatabaseConnectionFacade.getConnection();
      Optional<Account> o_acc = UserValidation.getAccountFromEmail(conn, email);
      if (o_acc.isPresent()) {
        Account acc = o_acc.get();
        try {
          if (PasswordHashingService.validatePassword(password, acc.password())) {
            if (acc.isActive()) {
              String displayName = acc.displayname();
              HttpSession session = request.getSession();

              session.setAttribute("email", email);
              session.setAttribute("displayName", displayName);
              session.setMaxInactiveInterval(30 * 60);
              session.setAttribute("account", acc);

              out.println("{\"status\": \"success\", \"displayName\": \"" + displayName + "\"}");
            } else {
              out.println("{\"status\": \"failed\" , \"description\": \"incorrect inputs\" }");
            }
          } else {
            out.println("{\"status\": \"failed\" , \"description\": \"incorrect inputs\" }");
          }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
          // These Errors should never be thrown
          e.printStackTrace();
        }
      } else {
        out.println("{\"status\": \"failed\" , \"description\": \"incorrect inputs\" }");
      }
      DatabaseConnectionFacade.releaseConnection(conn);
    } else {
      out.println("{\"status\": \"failed\", \"description\": \"no inputs\"  }");
    }
  }
}
