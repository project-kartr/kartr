package kartr.frontend;

import jakarta.servlet.*;
import java.time.*;
import java.util.*;

/*
 * Servlet responsible for regular logging of heap information.
 * Runs as long as the Deployment runs.
 */
public class MyContextListener implements ServletContextListener {
  Timer timer;

  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext ctx = servletContextEvent.getServletContext();

    ctx.log("initialized");

    TimerTask task =
        new TimerTask() {
          public void run() {
            long heapSize = (Runtime.getRuntime().totalMemory() / 1024 / 1024);
            long maxHeapSize = (Runtime.getRuntime().maxMemory() / 1024 / 1024);
            long freeHeapSize = (Runtime.getRuntime().freeMemory() / 1024 / 1024);

            ctx.log(
                "heapSize: "
                    + heapSize
                    + "MB maxHeapSize: "
                    + maxHeapSize
                    + "MB freeHeapSize: "
                    + freeHeapSize
                    + "MB");
          }
        };
    timer = new Timer("timer." + ctx.getContextPath(), true);
    timer.scheduleAtFixedRate(task, 0, 60000);
  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    ServletContext ctx = servletContextEvent.getServletContext();
    ctx.log("cancel timer");
    timer.cancel();
  }
}
