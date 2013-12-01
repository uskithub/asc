package jp.ascendia.Taschel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LogoutServlet extends BaseServlet {

	@Override
	protected void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println("[LogoutServlet][START] execute");
		
		HttpSession session = request.getSession(false);
		
		// セッションを破棄
		if (session != null) {
			session.invalidate();
		}

		this.getServletContext().getRequestDispatcher("/login.jsp").forward(request, response);
	}
}
