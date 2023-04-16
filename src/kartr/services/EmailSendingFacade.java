package kartr.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.util.Properties;

public class EmailSendingFacade {

  public static boolean sendMail(String message, String subject, String to, String from) {
    int errors = 0;
    Properties prop = new Properties();
    try {
      prop.load(EmailSendingFacade.class.getResourceAsStream("mail.properties"));
      if (prop.getProperty("mail.user") == null || prop.getProperty("mail.password") == null) {
        System.err.println(
            "EmailSendingFacade: mail.user or mail.password was null while loading"
                + " mail.properties");
        ++errors;
      }
    } catch (IOException e) {
      e.printStackTrace();
      ++errors;
    }

    if (errors == 0) {
      Session ses = Session.getInstance(prop, null);
      Message msg = new MimeMessage(ses);

      try {
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setText(message);
        Transport.send(msg, prop.getProperty("mail.user"), prop.getProperty("mail.password"));
      } catch (MessagingException e) {
        e.printStackTrace();
        ++errors;
      }
    }

    return errors == 0;
  }
}
