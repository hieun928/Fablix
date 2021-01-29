package server;

import java.sql.*;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.util.HashMap;



public class checkout extends HttpServlet{
	double price = 20.00;
	Connection connection;
	Statement mainStatement;
	String databaseUsername = MyConstants.databaseUsername;
	String databasePassword = MyConstants.databasePassword;
	
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
	
	
	private String[] parseCheckOutQuery(String[] queries)
	{
		String[] result = new String[3];
		result[0] = queries[0].split("=")[1];
		result[1] = queries[1].split("=")[1];
		result[2] = queries[2].split("=")[1];
		return result;
	}
	
	
	
	public void addToCart(String id, HashMap<String,Integer> cart)
	{
		if (cart.containsKey(id) == false)
			cart.put(id,1);
		else
			cart.put(id, cart.get(id) + 1);
	}
	
	private void updateCart(String id, int quantity, HashMap<String,Integer> cart)
	{
		if (quantity == 0)
			cart.remove(id);
		else
			cart.put(id,quantity);
	}
	

	
	public void updateCart(String[] queryInfo,HashMap<String,Integer> cart)
	{
	
		if (queryInfo[0].equals("add") )
			addToCart(queryInfo[1],cart);
		else if (queryInfo[0].equals("update"))
			updateCart(queryInfo[1],Integer.parseInt(queryInfo[2]),cart);
		else if (queryInfo[0].equals("remove"))
			cart.remove(queryInfo[1]);
		else if (queryInfo[0].equals("empty"))
			cart.clear();
	}
	
	
	
	
	private String getTitleBasedOnID(String id)
	{
		String result = "";
		try {
			ResultSet resultSet = mainStatement.executeQuery("SELECT * FROM movies m1 WHERE m1.id = '" + id +"'");
			if (resultSet.next())
				result += resultSet.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private double calculateGrandTotal(HashMap<String,Integer> cart)
	{
		double result = 0.00;
		for (String key : cart.keySet())
			result += price * cart.get(key);
		return result;
	}
	
	private String createHtmlForShoppingCart(HashMap<String,Integer> cart)
	{
		String result = "<head>" + htmlCreator.createCSSLink("navBar.css") + "</head><body>";
		result += htmlCreator.createMainNavBar();
		if (cart.keySet().size() == 0)
			result += "<center><h1 class=\"emptyDisplay\">Cart Is Empty</h1></center>";
		else
		{
			result += "<table>";
			result += "<table class = \"shoppingCart\" >";
			result += htmlCreator.createcheckOutTableHeader();
			for (String key : cart.keySet())
			{
				String title = getTitleBasedOnID(key);
				result += htmlCreator.createCheckOutItem(key, title, cart.get(key));
			}
			result += "</table></body>";
			result += "<h3> Grand Total: $ " + String.format("%.2f", calculateGrandTotal(cart)) + "</h3>";
			result += htmlCreator.createCheckOutScript();
			result += htmlCreator.createCheckoutAndEmptyButtons();
		}

		return result;
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	{
		initialize();
		HttpSession session = req.getSession(true);
		HashMap<String,Integer> cart = (HashMap<String,Integer>) session.getAttribute("cart");
		if (cart == null)
		{
			session.setAttribute("cart", new HashMap<String,Integer>());
			cart = (HashMap<String, Integer>) session.getAttribute("cart");
		}
		if (req.getQueryString() != null)
		{
		String[] queries = req.getQueryString().split("&");
			if (queries != null && queries.length != 0)
				updateCart( parseCheckOutQuery(queries), cart);
			}
		try {
			res.getWriter().write(createHtmlForShoppingCart(cart));
			mainStatement.close();
			connection.close();
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		} 
	}

}
