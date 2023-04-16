package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.util.UUID;
import kartr.model.PersistenceLayer;
import kartr.services.*;

/*
This servlet is used to handle password reset requests.
It first checks if the specified email exists in the database and sends an email with a link
to reset the password.
otherwise it sends a corresponding response to the client.
*/
@WebServlet("public/reset-password-request")
@MultipartConfig
public class ResetPasswordRequestServlet extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    response.setContentType("application/json");
    ServletContext context = this.getServletContext();
    String email = request.getParameter("email");

    ResponseHelper responseHelper = new ResponseHelper(response);

    Connection conn = DatabaseConnectionFacade.getConnection();

    // Email-Spoofing ?
    int accountId = PersistenceLayer.getAccountIdByEmail(conn, email);
    if (accountId <= 0) {
      responseHelper.sendErrorResponse("email does not exists");
      DatabaseConnectionFacade.releaseConnection(conn);
      return;
    }
    // Creation of a token from a combination of accountid, a random uuid and the current time (in
    // timestamp format)
    String resetPasswordToken =
        accountId + ":" + UUID.randomUUID().toString() + ":" + System.currentTimeMillis();
    // Generation of email content with a link to resetPaasword.html, where users can enter their
    // new passwords
    String message =
        String.format(
            "Hallo,\n"
                + "Sie haben sich gewünscht, Ihr Password zu ändern.\n"
                + "Klicken Sie bitte auf den folgen Link, um ein neues Password auszuwählen.\n\n"
                + "https://%1$s%2$s/resetPassword.html?token=%3$s\n\n"
                + "Sollten Sie dies nicht angefordert haben dann ignorieren Sie bitte diese"
                + " E-mail.\n\n"
                + "Viele Grüße\n"
                + "Ihr Kartr-Team",
            request.getServerName(), request.getContextPath(), resetPasswordToken);

    context.log("accountID " + accountId);
    context.log("resetPasswordToken " + resetPasswordToken);
    /* store the generated token in the database
       or generate an error response if the update failed
    */
    if (!PersistenceLayer.updatePasswordResetToken(conn, resetPasswordToken, accountId)) {
      responseHelper.createError("failed to update db", false);
    }
    DatabaseConnectionFacade.releaseConnection(conn);
    // send email
    EmailSendingFacade.sendMail(message, "Kartr Passwort zurücksetzen", email, "demo@demo.de");
    responseHelper.sendResponse("description", "An email has been sent");
  }
}
