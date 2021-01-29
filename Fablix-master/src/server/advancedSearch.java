package server;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class advancedSearch extends HttpServlet{
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	{
		String result = "<head>" + htmlCreator.createCSSLink("navBar.css") + "</head>";
		result += htmlCreator.createMainNavBar();
		result += "<center>" + htmlCreator.createAdvancedSearchHtml() + "</center>";
		try {
			res.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}


