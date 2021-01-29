package server;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;

import java.util.ArrayList;
import java.util.Collections;

public class searchFunctionsandDisplay extends HttpServlet
{
	int pageLimit = 15;
	HashMap<String,movieData> queryResult;

	
	
	private String [] splitQuery(String query)
	{
		String[] queries = query.split("&");
		return queries;
	}
	
	private searchObject createAdvanceSearchObject(String[] query)
	{
		searchObject infoBundle = new searchObject();
			for(int k = 4; k < query.length; ++k)
			{
				String[] input = query[k].split("=");
				if (input.length == 2)
				switch (input[0])
				{
				case "title": infoBundle.title = input[1];
					break;
				case "year" :infoBundle.year = Integer.parseInt(input[1]);
					break;
				case "director": infoBundle.director = input[1];
				   break;
				case "actorName": infoBundle.actorName = input[1];
				   break;
				case "subStringMatching": infoBundle.subStringSearch = true;					
					break;
				}
			}	
			return infoBundle;
		}
	
	
	ArrayList<String> sortByYear(HashMap<String,movieData> queries,String sortType)
	{
		ArrayList<String> sorted = new ArrayList<String>();
		for (String key : queries.keySet())
			sorted.add(queries.get(key).year + key);
		Collections.sort(sorted);
		if (sortType.equals("yearD"))
			Collections.reverse(sorted);
		for (int i = 0; i < sorted.size(); ++i)
			sorted.set(i, sorted.get(i).substring(4, sorted.get(i).length()));
		return sorted;
	}
	
	ArrayList<String> sortByTitle(HashMap<String,movieData> queries,String sortType)
	{
		ArrayList<String> sorted = new ArrayList<String>();
		for (String key : queries.keySet())
			sorted.add(key);
		Collections.sort(sorted);
		if (sortType.equals("titleD"))
			Collections.reverse(sorted);
		return sorted;
	}
	
	private String addNextPageButton(int pageNumber, int numberPerPage, int size, String queries)
	{
		System.out.println(queries);
		String result = "<center> <div class=\"pageNav\">";
		boolean shouldAddSeparator = false;
		if (pageNumber > 1 && size > numberPerPage){
			result += "<a href=\"/Fablix/searchPage?" + queries.replaceAll("page=[0-9]*\\&","page=" + (pageNumber - 1) + "&") + "\"> Previous Page </a>";
			shouldAddSeparator = true;
		}
		float maximumPage = ((float) size)/numberPerPage;
		maximumPage = (float) Math.ceil(maximumPage);
		if (pageNumber < ((int) maximumPage))
		{
			if (shouldAddSeparator)
			result += "<pre style=\"display:inline\">  </pre>";
			result += "<a href=\"/Fablix/searchPage?" + queries.replaceAll("page=[0-9]*\\&","page=" + (pageNumber + 1) + "&") + "\"> Next Page </a>";	
		}
		result += "</div></center>";
		return result;
	}
	
	private String createMovieInfoHtml(ResultSet res, int pageNumber, int numberPerPage, String sortMethod,boolean isEmpty, String queries) throws SQLException
	{
		String result = htmlCreator.createNavBarCssLink();
		result += htmlCreator.createMainNavBar();
		result += htmlCreator.createCSSLink("movieInfo.css");
		if (!isEmpty)
			result += htmlCreator.createSortButton(queries);
		movieListServlet.addData(res, queryResult,false,true);
		ArrayList<String> sorted;
		if (sortMethod.substring(0, sortMethod.length()-1).equals("title"))
			sorted = sortByTitle(queryResult,sortMethod);
		else
			sorted = sortByYear(queryResult,sortMethod);
		for (int i = (pageNumber * numberPerPage) - numberPerPage; (i < (pageNumber * numberPerPage) && i < sorted.size()); ++i)
		{
			String movieInput = sorted.get(i);
			movieData MovieData = queryResult.get(movieInput);
			result += htmlCreator.createMovieInfo(sorted.get(i), MovieData.year, MovieData.director, MovieData.actors, MovieData.genres,MovieData.movieID);
		}
		if(queryResult.isEmpty() == true)
		{
	       String noResults = htmlCreator.createNavBarCssLink();
		   noResults += htmlCreator.createMainNavBar();
		   noResults+= "<h1><center>No results Found</center></h1>";
		   return noResults;
		}
		else
		{
		   result +=addNextPageButton(pageNumber,numberPerPage,sorted.size(),queries);
		   result += htmlCreator.createNumPageButton(queries);
	       return result;
		}
	}
	
	
	String createSearchTitleGenre(int pageNumber,int numberPerPage,String searchMethod,String input,String sortMethod,String queries)
	{
		String result = "";
		search querySearch = new search();
		boolean isEmpty = true;
		try {
			ResultSet res;
			if(searchMethod.equals("searchTitle"))
			   res = querySearch.browseTitles(input);
			else
				res = querySearch.browseGenres(input);
			if (res.next() && res.next())
				isEmpty = false;
			res.beforeFirst();
			result += createMovieInfoHtml(res,pageNumber,numberPerPage,sortMethod,isEmpty,queries);
		querySearch.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private String createFullTextHtml(int pageNumber, int numberPerPage,String title, String sortMethod,String queries,LogWriter logwriter)
	{	
		String result = "";
		search querySearch = new search();
		boolean isEmpty = true;
		ResultSet res = null;
		long startQueryTime = System.nanoTime();

		try {
			res = querySearch.fullTextSearch(title);
			res.next();
			if (res.next() && res.next())
				isEmpty = false;
			res.beforeFirst();
			result += createMovieInfoHtml(res,pageNumber,numberPerPage,sortMethod,isEmpty,queries);
			long queryTime = System.nanoTime() - startQueryTime;
			logwriter.writeJDBCTime(queryTime);
		} catch (SQLException e) {
			e.printStackTrace();
		}finally
		{
		try {
			if (res != null)
				res.close();	
		} catch (SQLException e) {
			e.printStackTrace();}
		if (querySearch != null)
			querySearch.closeConnection();
		}
		return result;
	}
	
	
	private String createNormalSearchHtml(int pageNumber, int numberPerPage,String title, String sortMethod,String queries)
	{	
		String result = "";
		search querySearch = new search();
		boolean isEmpty = true;
		try {
			ResultSet res = querySearch.searchFunction(title, null, null, null, true);
			res.next();
			if (res.next() && res.next())
				isEmpty = false;
			res.beforeFirst();
			result += createMovieInfoHtml(res,pageNumber,numberPerPage,sortMethod,isEmpty,queries);
			querySearch.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
		
	}
	
	private String createAdvanceSearchHtml(int pageNumber, int numberPerPage,String sortMethod, searchObject advObj,String queries)
	{
		String result = "";
		search querySearch = new search();
		boolean isEmpty = true;
		try {
			ResultSet res = querySearch.searchFunction(advObj.title, advObj.year, advObj.director, advObj.actorName, advObj.subStringSearch);
			if (res.next() && res.next())
				isEmpty = false;
			res.beforeFirst();
			result += createMovieInfoHtml(res,pageNumber,numberPerPage,sortMethod,isEmpty,queries);
			querySearch.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	{
		LogWriter logWriter = new LogWriter();
		long startTime = System.nanoTime();
		
		queryResult = new HashMap<String,movieData>();
		String[] queries = splitQuery(req.getQueryString());
		int pageNumber = Integer.parseInt(queries[0].split("=")[1]);
		String method = queries[1].split("=")[1];
		int numPerPage = Integer.parseInt(queries[2].split("=")[1]);
		
		String sort = queries[3].split("=")[1];
		if (method.equals("searchTitle") || method.equals("searchGenre"))
		{
			String result = createSearchTitleGenre(pageNumber,numPerPage,method,queries[4].split("=")[1],sort,req.getQueryString());
			res.getWriter().write(result);
		}	
		//says normal search but calls on a full text search
		else if (method.equals("normalSearch"))
			res.getWriter().write(createFullTextHtml(pageNumber, numPerPage,queries[4].split("=")[1], sort, req.getQueryString(),logWriter));
		else if (method.equals("search"))
		{
			searchObject advanceSearchObject = createAdvanceSearchObject(queries);
			res.getWriter().write(createAdvanceSearchHtml(pageNumber, numPerPage, sort,advanceSearchObject,req.getQueryString()));
		}
		
		long totalResponseTime = System.nanoTime() - startTime;
		
		logWriter.writeServletTime(totalResponseTime);
		
	}
	
}
