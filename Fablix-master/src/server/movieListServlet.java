package server;

import java.sql.*;



import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;


public class movieListServlet extends HttpServlet{
	

	String databaseUsername = MyConstants.databaseUsername;
	String databasePassword = MyConstants.databasePassword;
	DataSource ds = null;

	
	private void setUp()
	{
		try 
		{
			// the following few lines are for connection pooling
            // Obtain our environment naming context
			// the following commented lines are direct connections without pooling
			//Class.forName("org.gjt.mm.mysql.Driver");
			//Class.forName("com.mysql.jdbc.Driver").newInstance();
			//Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
  
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
	protected void doGet(HttpServletRequest req, HttpServletResponse res) 
	{
		
		//Initialization area
		setUp();
		PreparedStatement statement = null;
		Connection connection = null;
     	try {
			connection = ds.getConnection();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String queryToExecute = "SELECT top20.title, top20.year, top20.director, top20.rating, genres.name, stars.name, top20.id\r\n" + 
				"FROM (SELECT movies.id, movies.title, movies.year, movies.director, ratings.rating FROM movies, ratings WHERE movies.id = ratings.movieId \r\n" + 
				"ORDER BY ratings.rating DESC LIMIT 20) AS top20, genres, genres_in_movies, stars, stars_in_movies\r\n" + 
				"WHERE  top20.id = genres_in_movies.movieId AND genres.id = genres_in_movies.genreid AND  top20.id = stars_in_movies.movieId AND stars.id = stars_in_movies.starid";
		try {
			statement = connection.prepareStatement(queryToExecute);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		//---------Initialization area

		HashMap<String,movieData> movieMap = new HashMap<String,movieData>();
		ResultSet resultSet = null;
		try {
			if (statement != null)
			{
			resultSet = Query(statement);
			addData(resultSet,movieMap,true,true);
			String html = createHTML(movieMap);
			res.getWriter().write(html);
			}

		} catch (SQLException | IOException e ) {
			e.printStackTrace();
		}finally
		{
			System.out.println("SHUTTING DOWN CONNECTION");
			try {
				if (resultSet != null)
					resultSet.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (statement != null)
					statement.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				if (connection != null)
					connection.close();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
}
	
	public static void addData(ResultSet result,HashMap<String,movieData> movieMap,boolean shouldAddRating,boolean shouldAddID) throws SQLException
	{
		while (result.next())
		{
			String title = result.getString(1);
			if (title != null)
			{
			if (!movieMap.containsKey(title))
				movieMap.put(result.getString(1), new movieData());
			movieData obj = movieMap.get(result.getString(1));
			obj.year = result.getInt(2);
			obj.director = result.getString(3);
			int index = 4;
			if (shouldAddRating)
				obj.rating = result.getDouble(index++);
			obj.actors.add(result.getString(index));
			obj.genres.add(result.getString(++index));
			if (shouldAddID)
				obj.movieID = result.getString(++index);
			}
		}
	}
	
	
	public ArrayList<String> sortByRating(HashMap<String,movieData> movieMap)
	{
		ArrayList<String> sortedMovies = new ArrayList<String>();
		for (String key : movieMap.keySet())
			sortedMovies.add(movieMap.get(key).rating + key);
		Collections.sort(sortedMovies);
		for (int i = 0; i < sortedMovies.size(); ++i)
			sortedMovies.set(i, sortedMovies.get(i).substring(3, sortedMovies.get(i).length()));
		Collections.reverse(sortedMovies);
		return sortedMovies;
	}
	
	//htmlResult += "<!DOCTYPE HTML> <html>\n <head><link rel="stylesheet" type="text/css" href="css/style.css"/></head> <body>\n";
	public String createHTML(HashMap<String,movieData> movieMap)
	{
		String htmlResult = "";
		htmlResult += "<!DOCTYPE HTML> <html>\n <head>"
				        + "<title>Fablix</title>"
				        + "<link rel = \"stylesheet\" type= \"text/css\" href = \"css/navBar.css\"/>"
				        + "<link rel = \"stylesheet\" type= \"text/css\" href = \"css/cards.css\"/>"
				        + "<link rel = \"stylesheet\" type= \"text/css\" href = \"css/searchOptions.css\"/>"
				        + "</head>";
		ArrayList<String> sortedMovies = sortByRating(movieMap);
		htmlResult += htmlCreator.createMainNavBar();
		htmlResult += "<h1 class=\"ratingTitle\">Highest Rated Movies</h1>";
		htmlResult += "<div class = \"containers\">";
		for (String key : sortedMovies)
		{
			movieData data = movieMap.get(key);
			htmlResult += createMovieCard.createCard(key, data.year, data.rating,data.movieID);
		}
		htmlResult += "</div> <div class=\"SearchNav\">" + htmlCreator.createGenresSearchLink() + htmlCreator.createTitleSearchLink() + "</div> </body> </html>";
		return htmlResult;
	}
	
	
	ResultSet Query(PreparedStatement statement) throws SQLException
	{
		return statement.executeQuery();
	}
}
