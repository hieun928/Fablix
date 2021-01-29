package server;
import java.sql.*;

import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;


public class _dashboard extends HttpServlet{
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

            connection  = ds.getConnection();
            if (connection == null)
                System.out.println("dbcon is null.");
			
         // Declare our statement
         mainStatement = connection.createStatement();
			
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	private String createDashboard()
	{
		String result="<h1><center>Employee Dashboard</center></h1>\n";
		ResultSet movie;
		try {
			movie = mainStatement.executeQuery("SELECT * from movies");
			result += htmlCreator.createMetaDataDisplay("Movie Table", movie.getMetaData());
			movie = mainStatement.executeQuery("SELECT * from stars");
			result += htmlCreator.createMetaDataDisplay("Stars Table", movie.getMetaData());
			movie = mainStatement.executeQuery("SELECT * from genres");
			result += htmlCreator.createMetaDataDisplay("Genres Table", movie.getMetaData());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	private String stripIncrementMax(String currentMax, int numToStrip)
	{
		String numberMax = currentMax.substring(numToStrip, currentMax.length());
		
		return  currentMax.substring(0,numToStrip) + (Integer.parseInt(numberMax) + 1);
	}
	
	
	private int insertStars(String starName, Integer year, String shouldForce)
	{
		boolean shouldExecute = true;
		try {
			if (shouldForce == null || shouldForce.equals("") || !shouldForce.equals("on"))
			{
				ResultSet result = mainStatement.executeQuery("SELECT * FROM stars s1 WHERE s1.name = '" + starName + "'");
					while (result.next())
					{
						Integer databaseYear = result.getInt(3);
						if ((databaseYear == 0 && year == null) || databaseYear.equals(year))
							return 1;
					}
			}
			if (shouldExecute)
			{
				ResultSet result = mainStatement.executeQuery("SELECT MAX(s1.id) FROM stars s1");
				result.next();
				String currentMax = result.getString(1);
				String newMax = stripIncrementMax(currentMax,2);
				String yearString = "NULL";
				if (year!=null)
					yearString = "'" + year.toString() + "'";
				mainStatement.execute("INSERT INTO stars VALUE('"+newMax+"','"+starName+"',"+yearString+")" );
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}	
	
	
	public String handleAddMovie(String movieID, String title, String year, String director, String starName, String genreName, String starID) throws SQLException
	{
	   //variables, prep for calling stored procedure
       PreparedStatement preparedStatement = null;
       String selectSQL = "CALL add_movie(?, ?, ?, ?, ?, ?, ?);";
	   preparedStatement = connection.prepareStatement(selectSQL);
	   System.out.println(starID);
	   //setting prepared statement
	   preparedStatement.setString(1, movieID);
	   preparedStatement.setString(2, title);
	   preparedStatement.setString(3, year);
	   preparedStatement.setString(4, director);
	   preparedStatement.setString(5, starName);
	   preparedStatement.setString(6, genreName );
	   preparedStatement.setString(7, starID);

	   // execute select stored procedure
	   ResultSet rs = preparedStatement.executeQuery();

	   //gets the first resultset message, not sure why not more, but gets first and thats all we need
		String message = "An error has occured.";
	    while (rs.next()) 
		{
			message = rs.getString(1);
			System.out.println(message);
		}
		return message;
	}
	
	
	public String getMovieStarId(String starName)
	{
		String nextMaxId = "";
		try {
			ResultSet result = mainStatement.executeQuery("SELECT s1.id FROM stars s1 WHERE s1.name = '" + starName + "'");
			if (result.next())
				return result.getString(1);
			else {
				ResultSet res = mainStatement.executeQuery("SELECT MAX(s1.id) FROM stars s1");
				res.next();
				nextMaxId = stripIncrementMax(res.getString(1),2);
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nextMaxId;
	}
	
	
	public String getMovieId(String movieName)
	{
		String nextMaxId = "";
		try {
			ResultSet result = mainStatement.executeQuery("SELECT m1.id FROM movies m1 WHERE m1.title = '" + movieName + "'");
			if (result.next())
				return result.getString(1);
			else {
				ResultSet res = mainStatement.executeQuery("SELECT MAX(m1.id) FROM movies m1");
				res.next();
				System.out.println("Movie ID: " + res.getString(1));
				nextMaxId = stripIncrementMax(res.getString(1),2);
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return nextMaxId;
	}
	
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res)
	{
		initialize();
		HttpSession session = req.getSession();
		Boolean employeeShouldLogin = (Boolean) session.getAttribute("employeeLogin");
		if (employeeShouldLogin == null || !employeeShouldLogin)
			try {
				res.sendRedirect("/Fablix/_dashboardLogin");
			}catch (IOException e) {
				e.printStackTrace();
			}
		else
			try {
				if (req.getParameter("starName") == null && req.getParameter("movieTitle") == null) {
					String htmlToWrite =  htmlCreator.createNavBarCssLink() + createDashboard() + htmlCreator.createStarsInsertForm(2) + htmlCreator.createAddMovieForm("");
					res.getWriter().write(htmlToWrite);
				}
				else {
					if (req.getParameter("movieTitle") == null) {
						String birthYear = req.getParameter("birthYear");
						Integer year = null;
						String force = req.getParameter("force");
						if (!(birthYear.equals("")))
							year = Integer.parseInt(birthYear);
						String htmlToWrite =  htmlCreator.createNavBarCssLink() + createDashboard();
						int resultCode = insertStars(req.getParameter("starName"),year,force);
						htmlToWrite += htmlCreator.createStarsInsertForm(resultCode) + htmlCreator.createAddMovieForm("");
						res.getWriter().write(htmlToWrite);
					}
					else if (req.getParameter("movieTitle") != null){
						String movieYear = req.getParameter("movieYear");
						String director = req.getParameter("movieDirector");
						try {
							String movieId = getMovieId(req.getParameter("movieTitle"));
							String message = handleAddMovie(movieId,req.getParameter("movieTitle"),movieYear,director,req.getParameter("movieStar"),req.getParameter("movieGenre"),getMovieStarId(req.getParameter("movieStar")));
							String htmlToWrite = htmlCreator.createNavBarCssLink() + createDashboard() + htmlCreator.createStarsInsertForm(2) + htmlCreator.createAddMovieForm(message);
							res.getWriter().write(htmlToWrite);
						} catch (SQLException e) {
							e.printStackTrace();
						}

					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}		
		try {
			connection.close();
			mainStatement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
