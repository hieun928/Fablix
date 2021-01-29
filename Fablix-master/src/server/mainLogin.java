package server;
import java.sql.*;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.sql.DataSource;


public class mainLogin extends HttpServlet{
	String Username = "test@gmail.com";
	String Password = "test";
	String databaseUsername = MyConstants.databaseUsername;
	String databasePassword = MyConstants.databasePassword;

	DataSource ds;
	
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
            
            // Look up our data source
           
            if (ds == null)
            {
            	ds = (DataSource) getServletContext().getAttribute("DBCPool");
                if (ds == null)
                    System.out.println("ds is null.");
            }	
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException 
	{
		HttpSession session = req.getSession(true);
		Boolean shouldLogin = (Boolean) session.getAttribute("shouldLogin");
		if (session.isNew() || shouldLogin == null || shouldLogin == false)
			res.getWriter().write(createLoginHtml(false,false));
		else
		{
			if (shouldLogin != null && shouldLogin)
				res.sendRedirect("/Fablix/fablixhome");
		}
	}
	
	protected String createLoginHtml(boolean failedLogin,boolean failedRecaptcha)
	{
		String loginHtml = "<!DOCTYPE html>"+
		"<html>" +
		"<head>" +
		"<title>Fablix Login</title>" +
		"<script src='https://www.google.com/recaptcha/api.js'></script>" + 
		"</head>" +
		"<style> body{background-image: url(\"http://1.bp.blogspot.com/-BVV947xlZtY/Vk4kkdo3III/AAAAAAAAFO4/unVH0li5jzg/s1600/20016_anime_scenery.jpg\");} </style>" +
		"<body>" +
		"<center> <h1> Fablix Login Page </h1> </center>" +
		"<center><form action=\"/Fablix/login\" method = \"POST\">";
		if (failedRecaptcha)
			loginHtml += "<p style=\"color:black\">Failed Recaptcha</p>";
		else if (failedLogin)
			loginHtml += "<p style=\"color:black\">Username or Password is Invalid</p>";
		loginHtml += "Email<br>" + 
			"<input type = \"email\" placeholder=\"Enter Email\" name =\"Email\" <Context path=\"/Fablix\" required><br>" +
			"Password<br>" +
			"<input type = \"password\" placeholder=\"Enter Password\" name=\"Password\" required>" +
//			"<div style=\"margin-top:1vh;\" class=\"g-recaptcha\" data-sitekey=\"" + MyConstants.SITE_KEY
//			+ "\"></div>" +
			"<p><button type = \"submit\">Login</button></center></form></body>";
		return loginHtml;
	
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res)
		throws IOException
	{

		HttpSession session = req.getSession(true);
		Boolean userShouldLogin = (Boolean) session.getAttribute("shouldLogin");
		if (userShouldLogin == null || userShouldLogin == false)
		{
			//IMPORTANT INITALIZATION AREA
			initialize();
			Connection con = null;
			Statement mainStatement = null;
		   	try {
				con = ds.getConnection();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		   	try {
				mainStatement = con.createStatement();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}	
		//-----------IMPORTANT INITALIZATION AREA
		String email = req.getParameter("Email");
		String password = req.getParameter("Password");
//		String recaptchaResponse = req.getParameter("g-recaptcha-response");

		ResultSet result = null;
		try {
			result = mainStatement.executeQuery("SELECT * FROM customers t1 WHERE t1.email = \"" + email + "\"");
	
			boolean failedLogin = true;
//			boolean failedRecaptcha = VerifyUtils.verify(recaptchaResponse);
				if (result.next()) {
//					if (email.equals(result.getString("email")) && password.equals(result.getString("password")) && failedRecaptcha)
					if (email.equals(result.getString("email")) && password.equals(result.getString("password")))
					{
						session.setAttribute("shouldLogin", true);
						res.sendRedirect("/Fablix/fablixhome");
						failedLogin = false;
					}
				
//				if (failedLogin || (!failedRecaptcha))
				}
				if (failedLogin)
				{
					session.setAttribute("shouldLogin",false);
//					res.getWriter().write(createLoginHtml(true,!failedRecaptcha));
					res.getWriter().write(createLoginHtml(true,false));
				}
				
			} catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			try {
				if (result != null)
					result.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (mainStatement != null)
					mainStatement.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (con != null)
					con.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		}
		else
		{
			res.sendRedirect("/Fablix/fablixhome");
		}
		
	}
}

