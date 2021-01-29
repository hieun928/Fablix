package server;
import java.sql.*;
import java.util.HashMap;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;


public class search 
{
	String databaseUsername = MyConstants.databaseUsername;
	String databasePassword = MyConstants.databasePassword;
	PreparedStatement statement = null;
	Connection connection = null;
	static DataSource ds = null;
	
	
	public search()
	{
		try 
		{
			// the following few lines are for connection pooling
            // Obtain our environment naming context
			// the following commented lines are direct connections without pooling
			//Class.forName("org.gjt.mm.mysql.Driver");
			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			
			if (ds == null)
			{
				Context initCtx = new InitialContext();
            	if (initCtx == null)
                	System.out.println("initCtx is NULL");
            	Context envCtx = (Context) initCtx.lookup("java:comp/env");
            	if (envCtx == null)
            		System. out.println("envCtx is NULL");
            	ds = (DataSource) envCtx.lookup("jdbc/moviedb");
            	if (ds == null)
                	System.out.println("ds is null.");
			}
			
            if (connection == null || connection.isClosed())
            	connection = ds.getConnection();
            
            if (connection == null)
                System.out.println("connection is null.");				
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	

	
	//for AND statements read more carefully, it didn't mean and is same category only different categories like their advanced search, made way easier
			public ResultSet searchFunction(String title, Integer year, String director, String actorName, boolean subStringMatching) throws SQLException
			{
			   ResultSet result;

			System.out.println(actorName);
			   if(title == null && year == null && director == null && actorName == null)
				   return null;
			   if(actorName != null)    
			      actorName = actorName.replaceAll("\\+"," ");
			   if(director != null)
			      director = director.replaceAll("\\+"," ");
			   if(title!= null)
			      title = title.replaceAll("\\+"," ");
				System.out.println(actorName);

		       //All the following if statements add needed where conditions to our base query, the count is to add AND to right places
			   String baseQuery = "SELECT  a1.title, a1.year, a1.director, s1.name, g2.name, a1.id  "
			       		+ "FROM genres g2, stars s1, genres_in_movies gm2, stars_in_movies sm1, "
			       		+ "(SELECT m1.title, m1.id, m1.director, m1.year FROM movies m1, stars s2, stars_in_movies sm2 WHERE sm2.starid = s2.id AND sm2.movieId = m1.id";
			       		
			       		
			       if(title != null && !title.isEmpty())
			       {
			          if(subStringMatching == true)
			          {
			    	     baseQuery += " AND m1.title LIKE ?" ;
			    	     title = "%" + title + "%";
			          }
			          else
			     	     baseQuery += " AND m1.title = ?" ; 
			  
			       }
			       if(year != null)
			          baseQuery += " AND m1.year = ?";
			       
			       if(director != null && !director.isEmpty())
			       {
			          if(subStringMatching == true)
			          {
			     	     baseQuery += " AND m1.director LIKE ?" ; 
			     	     director = "%" + director + "%";
			          }
			          else
			      	     baseQuery += " AND m1.director = ?" ; 
			   
			       }
			       if(actorName != null && !actorName.isEmpty())
			       {
			          baseQuery += " AND s2.name LIKE ?";
			          actorName = "%" + actorName + "%";
			       }
			       baseQuery += ") as a1 WHERE gm2.genreid = g2.id AND gm2.movieId = a1.id AND sm1.movieId = a1.id AND sm1.starid = s1.id";	
			       		
			  statement = connection.prepareStatement(baseQuery);
			  statement.setString(1, title);
			  System.out.println(baseQuery);
			  int currentValue = 2;
			  if (year != null)
			  {
				  statement.setInt(currentValue, year);
				  ++currentValue;
			  }
			  if (director != null)
			  {
				  statement.setString(currentValue, director);
				  ++currentValue;
			  }
			  if (actorName != null)
				  statement.setString(currentValue, actorName);
		      result = statement.executeQuery();
		      return result;
		   }
			
		
		
	
	public void addData(ResultSet result,HashMap<String,movieData> movieMap) throws SQLException
	{
		while (result.next())
		{
			if (!movieMap.containsKey(result.getString(1)))
				movieMap.put(result.getString(1), new movieData());
			movieData obj = movieMap.get(result.getString(1));
			obj.year = result.getInt(2);
			obj.director = result.getString(3);
			obj.genres.add(result.getString(4));
			obj.actors.add(result.getString(5));
		}
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
		
		String baseQuery = "SELECT  a1.title, a1.year, a1.director, s1.name, g2.name, a1.id  "
	       		+ "FROM genres g2, stars s1, genres_in_movies gm2, stars_in_movies sm1, "
	       		+ "(SELECT m1.title, m1.id, m1.director, m1.year FROM movies m1, stars s2, stars_in_movies sm2 WHERE sm2.starid = s2.id AND sm2.movieId = m1.id";

	    baseQuery += " AND MATCH (m1.title) AGAINST (? IN BOOLEAN MODE)";
	    baseQuery += ") as a1 WHERE gm2.genreid = g2.id AND gm2.movieId = a1.id AND sm1.movieId = a1.id AND sm1.starid = s1.id";	
        
	    //for debugging
	    System.out.println(baseQuery);
    	
	    
	    try 
	    {
	      statement = connection.prepareStatement(baseQuery);
	      statement.setString(1, title);
	      result = statement.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	
	

	public ResultSet browseGenres(String genre) throws SQLException
	{
	   ResultSet res;
	  
	   String genreQuery = "SELECT  a1.title, a1.year, a1.director, s1.name, g2.name, a1.id FROM stars s1, stars_in_movies sm1, genres g2, genres_in_movies gm2,"
	   		+ "(SELECT m1.title, m1.id, m1.director, m1.year FROM movies m1, genres g1, genres_in_movies gm1 WHERE gm1.genreid = g1.id AND gm1.movieId = m1.id AND g1.name = ?)";
       genreQuery = genreQuery + " as a1 WHERE gm2.genreid = g2.id AND gm2.movieId = a1.id AND sm1.movieId = a1.id AND sm1.starid = s1.id";
	   statement = connection.prepareStatement(genreQuery);
       statement.setString(1, genre);
	   res = statement.executeQuery();	      
	   return res;
	}
	
	
   
	public ResultSet browseTitles(String title) throws SQLException
	{ 
		String titleQuery ="SELECT m1.title, m1.year, m1.director, s1.name, g1.name, m1.id FROM movies m1, genres g1, stars s1, genres_in_movies gm1, stars_in_movies sm1"
				+ " WHERE gm1.genreid = g1.id AND gm1.movieId = m1.id AND sm1.movieId = m1.id AND sm1.starid = s1.id AND m1.title LIKE ?";		
		statement = connection.prepareStatement(titleQuery);
	    statement.setString(1, title + "%");
		ResultSet rs = statement.executeQuery();
		System.out.println(titleQuery);
		return rs;
	}
	
	

	public ResultSet searchStarInfo(String title) throws SQLException
	{
	   ResultSet res;
	   String starQuery = "SELECT s1.name, s1.id, s1.birthYear, m1.title FROM movies m1, stars s1, stars_in_movies sm1 WHERE sm1.movieId = m1.id AND sm1.starid = s1.id AND s1.name = ?";
	   statement = connection.prepareStatement(starQuery);
       statement.setString(1, title);
	   res = statement.executeQuery();
	   return res;
	}
	
	
	public ResultSet genreQuery() throws SQLException
	{
		String genreQuery = "SELECT * From genres";
		statement = connection.prepareStatement(genreQuery);
		return statement.executeQuery();
	}
	
	public void closeConnection()
	{
		try {
		if (statement != null)
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
		if (connection != null)
			connection.close();
		} catch (SQLException e) {
				e.printStackTrace();
		}
	}
}

