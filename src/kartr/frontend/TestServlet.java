package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Random;

@WebServlet("api/test")
public class TestServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    Random r = new Random();
    out.println("Hello" + r.nextInt(1000));

    ServletContext context = this.getServletContext();
    context.log("simple test");
  }
}
