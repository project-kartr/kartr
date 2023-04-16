package kartr.frontend;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import kartr.model.PersistenceLayer;
import kartr.services.DatabaseConnectionFacade;
import kartr.services.PasswordHashingService;
import kartr.services.ResponseHelper;

/*
This servlet is used to process requests to update passwords.
it first checks the existance of the  token and password parameters.
the token parameter should be a combination of an accountid, a uuid and the a timestamp separated by ":"
*/

@WebServlet("public/update-password")
@MultipartConfig
public class UpdatePasswordServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("application/json");
    String password = request.getParameter("password");
    String retype = request.getParameter("retype");
    String token = request.getParameter("token");

    ServletContext context = this.getServletContext();
    ResponseHelper responseHelper = new ResponseHelper(response);

    if (token == null || token == "") {
      responseHelper.sendErrorResponse("token does not exists");
      return;
    }

    if (password == null || retype == null) {
      responseHelper.sendErrorResponse("password or password retype is missing");
      return;
    }

    // compare the password and the confirmtion password (retype parameter)
    if (!password.equals(retype)) {
      responseHelper.sendErrorResponse("passwords are not equal");
      return;
    }

    // token regex validation
    String[] tokenArr = token.split(":");
    if (tokenArr.length <= 2) {
      responseHelper.sendErrorResponse("token is not valid");
      return;
    }

    int accountId = Integer.parseInt(tokenArr[0]);
    long expiredTime = Long.parseLong(tokenArr[2]);
    context.log("accountID: " + accountId);

    Connection conn = DatabaseConnectionFacade.getConnection();
    String storedToken =
        PersistenceLayer.getTokenByAccountId(conn, accountId, "reset_password_token");

    context.log("storedToken: " + storedToken);

    // check the token lifetime
    Boolean isExpired = System.currentTimeMillis() - expiredTime > 24 * 3600 * 1000;
    // check if the request token exists in the database
    if (storedToken != null && (!storedToken.equals(token) || isExpired)) {
      responseHelper.createError("token is not valid", true);
    }

    // Password Validation
    if (password.length() < 3) {
      responseHelper.createError("password is not safe", true);
    }

    // hashing the new password
    String hashedPassword = "";
    if (!responseHelper.hasErrors()) {
      try {
        hashedPassword = PasswordHashingService.generatePasswordHash(password);
      } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
        responseHelper.createError("The given password counldn't be hashed", true);
      }
    }

    if (!responseHelper.hasErrors()) {
      PersistenceLayer.updatePassword(conn, hashedPassword, accountId);
      PersistenceLayer.deleteToken(conn, accountId, "reset_password_token");
      responseHelper.sendResponse("description", "useless value");
    }

    DatabaseConnectionFacade.releaseConnection(conn);
  }
}
