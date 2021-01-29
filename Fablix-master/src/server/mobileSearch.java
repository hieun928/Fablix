package server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;

public class mobileSearch extends HttpServlet
{
	String databaseUsername = MyConstants.databaseUsername;
	String databasePassword = MyConstants.databasePassword;
	Statement mainStatement = null;
	Connection conn = null;
	JsonArray jsonArray;
	HashMap<String,movieData> Movies;

	
	
	private void initialize()
	{
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?autoReconnect=true&useSSL=false",databaseUsername,databasePassword);
			mainStatement = connection.createStatement();
			conn = connection;
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	

	private void addData(ResultSet result)
	{
		try {
			while (result.next())
			{
				if (!Movies.containsKey(result.getString(2)))
					Movies.put(result.getString(2), new movieData());
				movieData obj = Movies.get(result.getString(2));
				obj.movieID = result.getString(1);
				obj.year = result.getInt(3);
				obj.director = result.getString(4);
				obj.actors.add(result.getString(5));
				obj.genres.add(result.getString(6));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private JsonObject generateJsonObject(String title, String movieId, Integer year, String director, HashSet<String> Stars, HashSet<String> Genres) 
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("movieTitle", title);
		JsonObject additionalDataJsonObject = new JsonObject();	
		additionalDataJsonObject.addProperty("movieId",movieId);
		additionalDataJsonObject.addProperty("year", year);
		additionalDataJsonObject.addProperty("director", director);
		String stars = "";
		for (String star : Stars)
			stars += (star + ", ");
		stars = stars.substring(0,stars.length()-2);
		String genres = "";
		for (String genre: Genres)
			genres += (genre + ", ");
		genres = genres.substring(0,genres.length()-2);
		
		additionalDataJsonObject.addProperty("stars",stars);
		additionalDataJsonObject.addProperty("genres", genres);
		jsonObject.add("data", additionalDataJsonObject);
		return jsonObject;
	}
	
	private void addToJsonArray()
	{
		for (String title : Movies.keySet())
		{
			movieData obj = Movies.get(title);
			jsonArray.add(generateJsonObject(title,obj.movieID,obj.year,obj.director,obj.actors,obj.genres));
		}
	}
	
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException 
	{
	   initialize();
	   String title = req.getParameter("title");
	   System.out.println(title);
	   jsonArray = new JsonArray();
	   ResultSet results = fullTextSearch(title);
	   Movies = new HashMap<String,movieData>();
	   addData(results);
	   addToJsonArray();
	   res.getWriter().write(jsonArray.toString());
	}
	
	
	public ResultSet fullTextSearch(String title) 
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
		
		String baseQuery = "SELECT  a1.id, a1.title, a1.year, a1.director, s1.name, g2.name"
	       		+ " FROM genres g2, stars s1, genres_in_movies gm2, stars_in_movies sm1, "
	       		+ "(SELECT m1.title, m1.id, m1.director, m1.year FROM movies m1, stars s2, stars_in_movies sm2 WHERE sm2.starid = s2.id AND sm2.movieId = m1.id";

	    baseQuery += " AND MATCH (m1.title) AGAINST ('" + title + "' IN BOOLEAN MODE)";
	    baseQuery += ") as a1 WHERE gm2.genreid = g2.id AND gm2.movieId = a1.id AND sm1.movieId = a1.id AND sm1.starid = s1.id";	
        
	    //for debugging
	    System.out.println(baseQuery);
    	
	    
	    try {
			result = mainStatement.executeQuery(baseQuery);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	
	private void closeResources()
	{
		try {
			mainStatement.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}