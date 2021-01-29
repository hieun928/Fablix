package server;
import java.io.IOException;
import java.sql.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.util.HashMap;


public class autoComplete extends HttpServlet{
	Connection connection;
	Statement mainStatement;
	JsonArray jsonArray;
	private int starsCount = 0;
	private int movieCount = 0;
	
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
	
	private void closeResources()
	{
		try {
			mainStatement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void getMovieData(String query)
	{
		try {
//			ResultSet result = mainStatement.executeQuery("SELECT * FROM movies m1 WHERE m1.title LIKE '%"+query+"%' LIMIT 10");
			ResultSet result = titleSearch(query);
			for (int i = 1; result.next() != false; ++i)
			{
				movieCount = i;
				jsonArray.add(autoComplete.generateJsonObject(result.getString(1),result.getString(2), "Movies"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private void getStarData(String query)
	{
		try {
			HashMap<String,String> stars = new HashMap<String,String>();
//			ResultSet result = mainStatement.executeQuery("SELECT * FROM stars s1 WHERE s1.name LIKE '%"+query+"%' limit 10");
			ResultSet result = starSearch(query);
			for (int i = 1; result.next() != false; ++i)
			{
				starsCount = i;
				stars.put(result.getString(1), result.getString(2));
			}
			if (movieCount + starsCount > 10)
			{
				int i = 5;
				for (String star : stars.keySet())
				{
					if (i >= 10)
						break;
					if (movieCount > 5)
						jsonArray.remove(i-5);
					jsonArray.add(autoComplete.generateJsonObject(star,stars.get(star), "Stars"));
					++i;
				}
			}
			else
			{
				int i = 0;
				for (String star : stars.keySet())
				{
					if (i >= 10)
						break;
					jsonArray.add(autoComplete.generateJsonObject(star,stars.get(star), "Stars"));
					++i;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		initialize();
		jsonArray = new JsonArray();
		movieCount = 0;
		starsCount = 0;
		String query = req.getParameter("query");
		
		
		
		if (query == null || query.trim().isEmpty()) {
			res.getWriter().write(jsonArray.toString());
			return;
		}
		
		getMovieData(query);
		getStarData(query);
		
		res.getWriter().write(jsonArray.toString());
		closeResources();
	}
	
	private static JsonObject generateJsonObject(String id,String name, String category)
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("value", name);
		
		JsonObject additionalDataJsonObject = new JsonObject();	
		additionalDataJsonObject.addProperty("id",id);
		additionalDataJsonObject.addProperty("category", category);
	
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}
	
	//full text star search
	public ResultSet starSearch(String input) 
	{
		ResultSet result = null;
        StringBuilder inputConstruction = new StringBuilder(input);
	    inputConstruction.insert(0, '+');
	    input = inputConstruction.toString();
	    
	    
	    //consolidates extra spaces
	    for(int k = 1; k < input.length(); k++)
	    {
	        if(input.charAt(k) == '+' && input.charAt(k + 1) == '+')
	           inputConstruction.setCharAt(k, '^'); 
	    }
	  
	    input = inputConstruction.toString();
	    input = input.replace("^", "");
	    
	    //inserts needed spaces
	    StringBuilder secondRound = new StringBuilder(input);
	    int j = 0;
	    for(int k = 0; k < input.length(); k++, j++)
	    {
	       if(k < input.length() -1 && input.charAt(k) == '+')
	       {
	          secondRound.insert(j, ' ');
	          j++;
	       }
	    }
	    
	    input = secondRound.toString();
        System.out.println(secondRound);

        
		String baseQuery = "SELECT s1.id, s1.name FROM stars s1 WHERE MATCH (s1.name) AGAINST (? IN BOOLEAN MODE)";
	    //for debugging
	    System.out.println(baseQuery);
    
	    
	    try {
	    	PreparedStatement prepStatement = connection.prepareStatement(baseQuery);
	    	prepStatement.setString(1,input);
			result = prepStatement.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	//full text search on titles
	public ResultSet titleSearch(String title) 
	{
		
		ResultSet result = null;
        StringBuilder titleConstruction = new StringBuilder(title);
	    titleConstruction.insert(0, '+');
	    title = titleConstruction.toString();
	    
	    
	    //consolidates extra spaces
	    for(int k = 1; k < title.length(); k++)
	    {
	        if(title.charAt(k) == '+' && title.charAt(k + 1) == '+')
	           titleConstruction.setCharAt(k, '^'); 
	    }
	  
	    title = titleConstruction.toString();
	    title = title.replace("^", "");
	    
	    //inserts needed spaces
	    StringBuilder secondRound = new StringBuilder(title);
	    int j = 0;
	    for(int k = 0; k < title.length(); k++, j++)
	    {
	       if(k < title.length() -1 && title.charAt(k) == '+')
	       {
	          secondRound.insert(j, ' ');
	          j++;
	       }
	    }
	    
	    title = secondRound.toString();
        System.out.println(secondRound);
		
		String baseQuery = "SELECT m1.id, m1.title FROM movies m1 WHERE MATCH (m1.title) AGAINST (? IN BOOLEAN MODE)";
	    //for debugging
	    System.out.println(baseQuery);
    	
	    try {
	    	PreparedStatement prepStatement = connection.prepareStatement(baseQuery);
	    	prepStatement.setString(1, title);
			result = prepStatement.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
}
