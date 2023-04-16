package kartr.frontend;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

public class CSPFilter implements Filter {
  ServletContext ctx;

  public void init(FilterConfig config) throws ServletException {
    ctx = config.getServletContext();
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws java.io.IOException, ServletException {
    if (response instanceof HttpServletResponse) {
      HttpServletResponse hsr = (HttpServletResponse) response;
      String headerValues = "default-src 'self'; img-src 'self' data:;";
      hsr.setHeader("Content-Security-Policy", headerValues);
    }

    chain.doFilter(request, response);
  }

  public void destroy() {}
}
