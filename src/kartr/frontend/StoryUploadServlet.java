package kartr.frontend;

import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import kartr.model.Account;
import kartr.model.PersistenceLayer;
import kartr.services.DatabaseConnectionFacade;
import kartr.services.FileIdMatcher;
import kartr.services.FileStorageFacade;
import kartr.services.ResponseHelper;
import kartr.services.SanitizeHelper;

@WebServlet("/api/story-upload")
@MultipartConfig
public class StoryUploadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String headline = SanitizeHelper.sanitize(request.getParameter("headline"));
    String content = SanitizeHelper.sanitize(request.getParameter("content"));
    String poiIdString = request.getParameter("poi_id");
    String storyIdString = request.getParameter("story_id");
    String f = request.getParameter("files");

    int poiId = 0;
    int storyId = 0;

    ResponseHelper responseHelper = new ResponseHelper(response);
    Connection conn = DatabaseConnectionFacade.getConnection();
    Account acc = null;

    acc = getAccount(request.getSession(), responseHelper);

    if (acc == null) {
      // maybe not logged in? should not happen because of our filter
      System.err.println("Something is not right. the session attribute 'account' was null.");
      responseHelper.createError("not logged in");
    }

    if (content == null || content.isEmpty()) {
      responseHelper.createError("no content provided");
    }

    if (headline == null || headline.isEmpty()) {
      responseHelper.createError("no headline provided");
    }

    poiId = parseInt(poiIdString, responseHelper, "poi_id");

    if (storyIdString != null) { // only try to parse if provided
      storyId = parseInt(storyIdString, responseHelper, "story_id");
    }

    if (acc != null && !responseHelper.hasErrors()) {
      int accountId = acc.id();

      String[] files = null;
      if (f != null) {
        files = f.split(",");
      }

      boolean updatedStory = false;

      if (!responseHelper.hasErrors() && poiId > 0 && accountId > 0) {
        // if storyId is already a greater-than-zero id, try to update story, else add story
        if (storyId > 0) {
          if (acc.isAdmin() || accountId == PersistenceLayer.getStoryAuthorId(conn, storyId)) {
            if (!PersistenceLayer.updateStory(conn, storyId, headline, content, accountId, poiId)) {
              responseHelper.createError("could not update story");
            } else {
              updatedStory = true;
            }
          } else {
            responseHelper.createError("you are not allowed to change this story");
          }
        } else {
          storyId = PersistenceLayer.addStory(conn, headline, content, accountId, poiId);
        }

        if (!responseHelper.hasErrors() && storyId > 0 && files != null) {
          // addStory was successful and there are files to be added
          addFiles(conn, responseHelper, updatedStory, files, storyId);
        }
      }
    }

    DatabaseConnectionFacade.releaseConnection(conn);

    responseHelper.sendResponse("story_id", storyId);
  }

  private void addFiles(
      Connection conn,
      ResponseHelper responseHelper,
      boolean updatedStory,
      String[] files,
      int storyId) {
    if (updatedStory) {
      PersistenceLayer.clearFilesForStory(conn, storyId);
    }

    FileIdMatcher fidm = new FileIdMatcher();
    for (String fileId : files) {
      if (fidm.matches(fileId)) {
        // valid file_id
        FileStorageFacade fs = new FileStorageFacade();
        String mimeType = fs.getMimeType(fileId);

        if (mimeType != null) {
          if (!PersistenceLayer.insertFile(conn, fileId, storyId, mimeType)) {
            responseHelper.createError("could not add file to story");
          }
        } else {
          responseHelper.createError(
              "file '" + fileId + "' does not exist. please upload your files beforehand");
        }
      } else {
        // invalid name, probably dangerous
        responseHelper.createError("what are you doing? stop it! (filename not allowed)");
        break; // someone's probably trying to break the application
      }
    }
  }

  private Account getAccount(HttpSession session, ResponseHelper responseHelper) {
    Account acc = null;
    try {
      acc = (Account) session.getAttribute("account");
    } catch (ClassCastException e) {
      e.printStackTrace();
      responseHelper.createError("could not get Account object from session", false);
    }
    return acc;
  }

  private int parseInt(String num, ResponseHelper responseHelper, String name) {
    int tmp = 0;
    try {
      tmp = Integer.parseInt(num);
    } catch (NumberFormatException e) {
      e.printStackTrace();
      responseHelper.createError(name + " was not an integer");
    }
    return tmp;
  }
}
