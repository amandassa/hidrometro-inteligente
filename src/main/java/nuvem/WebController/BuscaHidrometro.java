/**
 * Criado em 1 de set de 2022
 */
package nuvem.WebController;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Amanda Silva
 */
@WebServlet(urlPatterns = "/BuscaHidrometro")
public class BuscaHidrometro extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		PrintWriter writer = resp.getWriter();
		resp.setStatus(200);
		resp.setHeader("Content-Type", "text/plain");
		
//		resp.getOutputStream().println(controller.getHidrometro(tal));
		
		writer.close();
		// TODO Auto-generated method stub
		//super.doGet(req, resp);
	}
}
