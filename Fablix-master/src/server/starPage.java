package server;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



public class starPage extends HttpServlet
{
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
	    String name;
	    String id;
	    int birthDate;
	    HashSet<String> titles = new HashSet<String>();
		
		
		String queryResult;
		ResultSet rs;
		String result = htmlCreator.createNavBarCssLink();
		result += htmlCreator.createMainNavBar();
		result += htmlCreator.createCSSLink("movieInfo.css");
		
		queryResult = req.getQueryString();
		String[] queries = queryResult.split("=");
		queryResult = queries[1];
		if(queryResult!= null)
		{
			   queryResult = queryResult.replaceAll("^\\s+", "");
		}
		
       HashSet<String> set = new HashSet<String>();
        queryResult = queryResult.replaceAll("%20"," ");
       Matcher m = Pattern.compile("%..").matcher(queryResult);
       while (m.find())
           set.add(m.group().substring(1,m.group().length()));
       for (String key : set)
           queryResult = queryResult.replaceAll("%"+key,Character.toString((char) (Integer.parseInt(key,16))));
       queryResult = queryResult.trim();
		
		
		search starSearch = new search();
		try 
		{
		   rs = starSearch.searchStarInfo(queryResult);
			
           rs.next();
	       name = rs.getString(1);
	       id = rs.getString(2);
	       birthDate = rs.getInt(3);
           titles.add(rs.getString(4));
           while(rs.next())
              titles.add(rs.getString(4));

           
           result  += htmlCreator.createStarInfo(name, id, birthDate,titles);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        res.getWriter().write(result);	
	}
}