package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;

/*
 * Servlet responsible for logging the real client IPs, because the proxy
 * would otherwise hide them.
 *
 * Passes the request on to the other filters.
 */
public class MyFilter implements Filter {
  ServletContext ctx;

  public void init(FilterConfig config) {
    ctx = config.getServletContext();
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest hsr = (HttpServletRequest) request;
      String forwardedFor = hsr.getHeader("X-Forwarded-For");
      ctx.log("IP " + forwardedFor);
    }

    // don't propagate exceptions to users
    try {
      chain.doFilter(request, response);
    } catch (Exception e) {
      e.printStackTrace();
      if (response instanceof HttpServletResponse resp) {
        resp.reset();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  public void destroy() {}
}
