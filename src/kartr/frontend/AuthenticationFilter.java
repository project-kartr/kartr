package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;

public class AuthenticationFilter implements Filter {
  ServletContext ctx;

  public void init(FilterConfig config) throws ServletException {
    ctx = config.getServletContext();
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    /*
     * Check if request is from a logged in client.
     * The filter is setup in web.xml to filter all calls to /api/*
     */

    if (request instanceof HttpServletRequest req) {
      String uri = req.getRequestURI();
      ctx.log("Requested Resource: " + uri);
      HttpSession session = req.getSession(false);
      if (session == null) {
        ctx.log("no valid session");
        if (response instanceof HttpServletResponse resp) {
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
      } else {
        if (session.getAttribute("account") != null) {
          chain.doFilter(request, response);
        } else {
          if (response instanceof HttpServletResponse resp) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
          }
        }
      }
    } // else don't serve anything
  }

  public void destroy() {}
}
