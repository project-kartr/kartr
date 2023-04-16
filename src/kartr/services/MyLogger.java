package kartr.services;

import java.util.logging.*;

public class MyLogger {
  public static synchronized void info(String msg) {
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, msg);
  }
}
