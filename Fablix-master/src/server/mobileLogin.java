
package server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class mobileLogin extends HttpServlet
{
	String databaseUsername = MyConstants.databaseUsername;
	String databasePassword = MyConstants.databasePassword;
	Statement mainStatement = null;
	Connection con;

	
	private void initialize()
	{
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false",databaseUsername,databasePassword);
			mainStatement = connection.createStatement();
			con = connection;
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException 
	{
	   initialize();
	   String email = req.getParameter("Email");
	   String password = req.getParameter("Password");
	   
	  try 
	  {
         ResultSet result = mainStatement.executeQuery("SELECT * FROM customers t1 WHERE t1.email = \"" + email + "\"");
		 if (result.next()) 
		 {
		    if (email.equals(result.getString(6)) && password.equals(result.getString(7)))
		    {
		    	res.getWriter().write("true");
		 		return;
		    }
		 }
		 res.getWriter().write("false");
		  
	   } 
	   catch (SQLException e) {e.printStackTrace();}
	   try 
	   {
	      mainStatement.close();
	      con.close();
	   } catch (SQLException e) {e.printStackTrace();}
	}
	
	
}
	
	