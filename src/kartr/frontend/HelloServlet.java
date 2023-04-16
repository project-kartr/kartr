package kartr.frontend;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Random;

/*
The HelloServlet is used to test if the deployment was succesfull.
The makeanddeploy.sh skript that is used to deploy calls the
servlet and checks if the response is good.
*/

public class HelloServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    response.setContentType("text/plain");
    PrintWriter out = response.getWriter();
    Random r = new Random();
    out.println("Hello" + r.nextInt(1000));

    ServletContext context = getServletContext();
    context.log("simple logging");
  }
}
