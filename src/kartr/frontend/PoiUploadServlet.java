package kartr.frontend;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import kartr.model.Account;
import kartr.model.PersistenceLayer;
import kartr.services.DatabaseConnectionFacade;
import kartr.services.ResponseHelper;
import kartr.services.SanitizeHelper;

/*
This is a Java servlet that handles HTTP POST requests sent to the "/api/poi-upload" endpoint.
The servlet expects a multipart/form-data request that contains latitude, longitude, displayname,
and poi_id parameters. The request body is used to create or update a point of interest (POI)
record in a database. The servlet uses the PersistenceLayer and DatabaseConnectionFacade
to communicate with the database.
*/

@WebServlet("/api/poi-upload")
@MultipartConfig
public class PoiUploadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    ResponseHelper responseHelper = new ResponseHelper(response);
    // longitude:<number>,latitude:<number>,displayname:<text>
    Connection conn = DatabaseConnectionFacade.getConnection();

    String longitude = request.getParameter("longitude");
    String latitude = request.getParameter("latitude");
    String displayname = SanitizeHelper.sanitize(request.getParameter("displayname"));
    String poiIdString = request.getParameter("poi_id");

    BigDecimal lon = null;
    BigDecimal lat = null;
    int accountId = 0;

    Account acc = null;
    HttpSession session = request.getSession();

    try {
      // try getting account object
      acc = (Account) session.getAttribute("account");
      if (acc == null) {
        responseHelper.createError("account not set", true);
      } else {
        accountId = acc.id();
      }
    } catch (ClassCastException e) {
      e.printStackTrace();
      responseHelper.createError(
          "account session attribute was not of type kartr.model.Account", false);
    }

    try {
      // try parsing numbers
      lon = new BigDecimal(longitude);
      lat = new BigDecimal(latitude);
      // check if numbers are in ]-1000;1000[
      if (!checkNumbersBetweenBounds(-1000, 1000, lon, lat)) {
        responseHelper.createError(
            "lon and lat numbers not in bounds (]-1000;1000[) with: lon=" + lon + " lat=" + lat,
            true);
      }
    } catch (NumberFormatException ignored) {
      responseHelper.createError("provided lon and/or lat strings were no valid numbers", true);
    }

    int poiId = 0;
    // use existing poiId if it exists; edit poi insted of creating a new one
    try {
      if (poiIdString != null) {
        poiId = Integer.parseInt(poiIdString);
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    }

    // if no errors happened, and lattitude, longitude and accoundId are not null.
    if (!responseHelper.hasErrors()) {
      if (lat != null && lon != null && accountId != 0) {
        // if poiId is bigger than 0, update the existing poi
        if (poiId > 0) {
          // if the update failed, then create an error
          if (!PersistenceLayer.updatePoi(conn, poiId, lon, lat, accountId, displayname)) {
            responseHelper.createError("updatePoi failed", true);
          }
        } else {
          poiId = PersistenceLayer.addPoi(conn, lon, lat, accountId, displayname);
        }
      } else {
        responseHelper.createError("could not create poi", true);
      }
    }

    DatabaseConnectionFacade.releaseConnection(conn);
    responseHelper.sendResponse("poi_id", poiId);
  }

  private boolean checkNumbersBetweenBounds(int lower, int upper, BigDecimal lon, BigDecimal lat) {
    // check if numbers are in ]lower;upper[
    if (lon.compareTo(BigDecimal.valueOf(upper)) == -1
        && lat.compareTo(BigDecimal.valueOf(upper)) == -1
        && lon.compareTo(BigDecimal.valueOf(lower)) == 1
        && lat.compareTo(BigDecimal.valueOf(lower)) == 1) {
      // is ok
      return true;
    } else {
      // is not ok
      return false;
    }
  }
}
