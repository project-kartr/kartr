package kartr.services;

import jakarta.json.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import kartr.model.AppException;

public class ResponseHelper {
  private ArrayList<Exception> errors;
  private ArrayList<Exception> infos;
  private ArrayList<Exception> warnings;
  private HttpServletResponse response;
  private JsonBuilderFactory factory = Json.createBuilderFactory(null);

  public ResponseHelper(HttpServletResponse response) {
    this.response = response;
    this.errors = new ArrayList<Exception>();
    this.infos = new ArrayList<Exception>();
    this.warnings = new ArrayList<Exception>();
  }

  public void sendResponse(String fieldDescription, int fieldId) throws IOException {
    sendResponse(fieldDescription, Integer.toString(fieldId));
  }

  public void sendResponse(String fieldDescription, String fieldId) throws IOException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    JsonObjectBuilder jsonResponse = factory.createObjectBuilder();

    if (errors.size() == 0) {
      jsonResponse.add("status", "success");
      jsonResponse.add(fieldDescription, fieldId);
      if (warnings.size() != 0 || infos.size() != 0) {
        JsonArray messages = createMessagesArray();
        jsonResponse.add("messages", messages);
      }
    } else {
      JsonArray messages = createMessagesArray();
      jsonResponse.add("status", "failed");
      jsonResponse.add("messages", messages);
    }

    out.println(jsonResponse.build());
    errors.clear();
  }

  public void sendErrors() throws IOException {
    response.setContentType("application/json");
    PrintWriter out = response.getWriter();
    JsonObjectBuilder jsonResponse = factory.createObjectBuilder();

    if (errors.size() != 0) {
      JsonArray messages = createMessagesArray();
      jsonResponse.add("status", "failed");
      jsonResponse.add("messages", messages);
    }

    out.println(jsonResponse.build());
    errors.clear();
  }

  public void sendErrorResponse(String errorDescription) throws IOException {
    PrintWriter out = response.getWriter();
    out.println("{\"status\": \"failed\", \"description\": \"" + errorDescription + "\"}");
  }

  public Exception createError(String msg) {
    AppException e = new AppException(msg, true);
    this.errors.add(e);
    return e;
  }

  public Exception createError(String msg, boolean publicError) {
    AppException e = new AppException(msg, publicError);
    this.errors.add(e);
    return e;
  }

  public Exception createInfo(String msg) {
    return createInfo(msg, true);
  }

  public Exception createInfo(String msg, boolean publicError) {
    AppException e = new AppException(msg, publicError);
    this.infos.add(e);
    return e;
  }

  public Exception createWarning(String msg) {
    return createWarning(msg, true);
  }

  public Exception createWarning(String msg, boolean publicError) {
    AppException e = new AppException(msg, publicError);
    this.warnings.add(e);
    return e;
  }

  public int getErrorsSize() {
    return this.errors.size();
  }

  public boolean hasErrors() {
    return (this.errors.size() > 0);
  }

  private JsonArray createMessagesArray() {
    JsonArrayBuilder arrayBuilder = factory.createArrayBuilder();
    addMessagesToArray(arrayBuilder, errors, "error");
    addMessagesToArray(arrayBuilder, warnings, "warning");
    addMessagesToArray(arrayBuilder, infos, "info");
    return arrayBuilder.build();
  }

  private void addMessagesToArray(
      JsonArrayBuilder arrayBuilder, ArrayList<Exception> list, String msgType) {
    for (Exception msg : list) {
      if (msg instanceof AppException appEx) {
        if (appEx.isPublic()) {
          JsonObjectBuilder msgObj = factory.createObjectBuilder();
          msgObj.add("type", msgType);
          msgObj.add("content", msg.getMessage());
          arrayBuilder.add(msgObj);
        }
      } else {
        System.err.println(
            "ResponseHelper: got Exception in errors list that's not of type "
                + "AppException: "
                + msg.toString());
        msg.printStackTrace();
      }
    }
  }
}
