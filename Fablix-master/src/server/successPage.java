
package server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class successPage extends HttpServlet
{
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		String result;
		result = htmlCreator.createNavBarCssLink();
		result += htmlCreator.createMainNavBar();
		result += htmlCreator.createCSSLink("movieInfo.css");
		
		if (req.getQueryString() != null)
		{
			String queryResult = req.getQueryString();
			String[] queries = queryResult.split("=");
			queryResult = queries[1];
		
			if(queryResult.equals("true"))
				result+= "<h1><center>Successful Transaction Confirmed.  Thank you for your business.</center></h1>";
			else
				result+= "<h1><center>Transaction Failed. Customer not found in database.</center></h1>";
	    
			res.getWriter().write(result);
		}
		else 
			res.sendRedirect("/Fablix/checkingOut");
	}
}
