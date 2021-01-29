package server;



import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.util.Date;



public class checkingOut extends HttpServlet {
	String databaseUsername = MyConstants.databaseUsername;
	String databasePassword = MyConstants.databasePassword;
	
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
            DataSource ds = (DataSource) envCtx.lookup("jdbc/moviedbWrite");
            

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
	
	private void closeConnection()
	{
		try {
			connection.close();
			mainStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private ResultSet getQueryResult(String queryString)
	{
		ResultSet result = null;
		try {
			result = mainStatement.executeQuery("SELECT * FROM creditcards c1 WHERE c1.id = '" + queryString + "'");
			if (!result.next())
				return null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String checkForQueryError(ResultSet rs,String[] queries)
	{
		if (rs == null)
			return "Credit Card Enter Is Invalid";
		else
			try {
				if (!(rs.getString(2).toLowerCase().equals(queries[0].toLowerCase())))
					return "Invalid First name For The Credit Card Entered: " + queries[0];
				else if (!(rs.getString(3).toLowerCase().equals(queries[1].toLowerCase())))
					return "Invalid Last name For The Credit Card Entered: " + queries[1];
				else if (!(rs.getString(4).equals(queries[3])))
					return "Invalid Expiration Date For The Credit Card Entered: " + queries[3];
			} catch (SQLException e) {
				e.printStackTrace();
			}
		return "false";
	}
	
	private String[] getQueries(String[] queryInfo)
	{
		String[] queries = new String[4];
		for (int i = 0; i < 4; ++i)
			queries[i] = queryInfo[i].split("=")[1];
		return queries;
	}
	
	
	private boolean handleSuccessfulPurchase(String[] queries,HashMap<String,Integer> cart)
	{
		try {
			ResultSet idResult = mainStatement.executeQuery("SELECT MAX(s1.id) FROM sales s1");
			idResult.next();
			int salesID = idResult.getInt(1) + 1;
			ResultSet customerID = mainStatement.executeQuery("SELECT c1.id FROM customers c1 WHERE c1.firstName = '" + queries[0] +"' AND c1.lastName = '" + queries[1] + "' AND c1.ccId = '" + queries[2] + "'");
			if (!customerID.next())
				return false;
			double customerId = customerID.getDouble(1);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
			String date = dateFormat.format(new Date());
			
			for (String key: cart.keySet())
			{
				System.out.println(key);
				for (int i = 0; i < cart.get(key); ++i,++salesID)
				{
					System.out.println("INSERT INTO sales ('" + salesID + "','" + customerId + "','" + key + "','"+ date + "')");
					mainStatement.execute("INSERT INTO sales VALUES('" + salesID + "','" + customerId + "','" + key + "','"+ date + "')");
				}
			}
			cart.clear();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	{
		HttpSession session = req.getSession();
		HashMap<String,Integer> cart = (HashMap<String, Integer>) session.getAttribute("cart");
		String page = "<head>" + htmlCreator.createNavBarCssLink() + "</head>";
		page += htmlCreator.createMainNavBar();
		if (cart == null || cart.size() == 0)
			try {
				res.sendRedirect("/Fablix/checkOut");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		else {
		if (req.getQueryString() == null)
			try {
				res.getWriter().write(page + htmlCreator.createCheckOutForm(null));
			} catch (IOException e) {
				e.printStackTrace();
			}
		else {

			String[] queryInfo = req.getQueryString().split("&");
			initialize();
			String[] queries = getQueries(queryInfo);
			for (int i = 0; i < 4; ++i)
				queries[i] = queries[i].replaceAll("%s", "");
		
			String error = checkForQueryError(getQueryResult(queries[2]), queries);
				try {
					if (error != "false")
						res.getWriter().write(page + htmlCreator.createCheckOutForm(error));
					else
					{
						boolean success = handleSuccessfulPurchase(queries,cart);
						res.sendRedirect("/Fablix/successPage?success="+success);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			closeConnection();
		}
	}
}
}
