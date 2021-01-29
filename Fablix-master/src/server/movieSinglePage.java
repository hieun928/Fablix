package server;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class movieSinglePage extends HttpServlet
{
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		String queryResult;
		ResultSet rs;
		String result = htmlCreator.createNavBarCssLink();
		result += htmlCreator.createMainNavBar();
		result += htmlCreator.createCSSLink("movieInfo.css");
		
		queryResult = req.getQueryString();
		String[] queries = queryResult.split("=");
		queryResult = queries[1];
		
		
	      HashSet<String> set = new HashSet<String>();
	        queryResult = queryResult.replaceAll("%20"," ");
	       Matcher m = Pattern.compile("%..").matcher(queryResult);
	       while (m.find())
	           set.add(m.group().substring(1,m.group().length()));
	       for (String key : set)
	           queryResult = queryResult.replaceAll("%"+key,Character.toString((char) (Integer.parseInt(key,16))));
	       queryResult = queryResult.trim();
			
		
		
		search titleSearch = new search();
		try {
			rs = titleSearch.searchFunction(queryResult,null,null,null,false);
	        movieData obj = new movieData();
	        rs.next();
	        obj.year = rs.getInt(2);
			obj.director = rs.getString(3);
			obj.genres.add(rs.getString(5));
		    obj.movieID = rs.getString(6);
            obj.actors.add(rs.getString(4));

	           while(rs.next())
	           {
	              obj.genres.add(rs.getString(5));
			      obj.actors.add(rs.getString(4));
	           }
		    result  += htmlCreator.createMovieInfo(queryResult, obj.year, obj.director, obj.actors, obj.genres,obj.movieID);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	  

        res.getWriter().write(result);	
	}
}
