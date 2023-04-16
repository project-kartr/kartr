package kartr.frontend;

import jakarta.json.JsonObject;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.Connection;
import java.util.*;
import kartr.model.*;
import kartr.services.*;

@WebServlet("/public/get-list-of-pois")
public class GetListOfPois extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();

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

    Connection conn = DatabaseConnectionFacade.getConnection();
    List<PointOfInterest> pois = PersistenceLayer.findAllPointOfInterest(conn);
    JsonObject json = PointOfInterestConversion.convertToJsonWithStatus(pois, "success", accId);
    DatabaseConnectionFacade.releaseConnection(conn);

    out.println(json.toString());
  }
}
