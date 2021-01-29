package server;

import java.io.IOException;
import java.sql.*;
import javax.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.coyote.RequestGroupInfo;

public class _dashboardLogin extends HttpServlet{
	Connection connection;
	Statement mainStatement;
	
	private void initialize()
	{
		try 
		{
			// the following few lines are for connection pooling
            // Obtain our environment naming context
			// the following commented lines are direct connections without pooling
			//Class.forName("org.gjt.mm.mysql.Driver");
			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			//Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

            Context initCtx = new InitialContext();
            if (initCtx == null)
                System.out.println("initCtx is NULL");

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
               System. out.println("envCtx is NULL");
            
            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            

            if (ds == null)
                System.out.println("ds is null.");

            connection = ds.getConnection();
            if (connection == null)
                System.out.println("dbcon is null.");
			
         // Declare our statement
         mainStatement = connection.createStatement();
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private String createLoginPage(boolean shouldLogin)
	{
		String result = "<html>"
				+ "<head>"
				+  htmlCreator.createNavBarCssLink()
				+ "</head>"
				+ "<body>"
				+ "<center><h1>Employee Login</h1></center>";
				if (!shouldLogin)
					result += "<center><h2>Username or Password is Invalid</h2></center>";
				result += "<center><form action=\"/Fablix/_dashboardLogin\" method=\"POST\">"
				+ "<h4>Email:</h4>"
				+ "<input type=\"email\" placeholder=\"Enter Email\" name=\"employeeEmail\" required></input>"
				+ "<h4>Password:</h4>"
				+ "<input type=\"password\" placeholder=\"Enter Password\" name=\"employeePass\" required></input>"
				+ "<center><button type=\"submit\">Login</center>"
				+ "</form></center></body></html>";
		return result;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	{
		initialize();
		HttpSession session = req.getSession();
		Boolean employeeShouldLogin = (Boolean) session.getAttribute("employeeLogin");
		if (employeeShouldLogin != null && employeeShouldLogin)
			try {
				res.sendRedirect("/Fablix/_dashboard");
			} catch (IOException e) {
				e.printStackTrace();
			}
		else
			try {
				res.getWriter().write(createLoginPage(true));
			} catch (IOException e) {
				e.printStackTrace();
			}
		try {
			mainStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
	{
		initialize();
		HttpSession session = req.getSession();
		String email = req.getParameter("employeeEmail");
		String password = req.getParameter("employeePass");
		try {
			ResultSet result = mainStatement.executeQuery("SELECT * FROM employees e1 WHERE e1.email = '" + email + "'");
			if (result.next() && result.getString(1) != null && result.getString(2).equals(password)){
				session.setAttribute("employeeLogin", true);
				res.sendRedirect("/Fablix/_dashboard");
			}
			else{
				session.setAttribute("employeeLogin", false);
				res.getWriter().write(createLoginPage(false));
			}
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
		try {
		mainStatement.close();
		connection.close();
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
}
