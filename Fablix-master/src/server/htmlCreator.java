package server;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import java.sql.ResultSetMetaData;
import java.io.BufferedReader;
import java.io.File;

public class htmlCreator {
	
	public static String createCSSLink(String title)
	{
		return "<link rel=\"stylesheet\" type=\"text/css\" href=\"css/" + title + "\">";
	}
	
	public static String createNavBarCssLink()
	{
		return "<head>"
				+ htmlCreator.createCSSLink("navBar.css")
				+ "</head>";
	}
	
	public static String createMainNavBar()
	{
		String result = htmlCreator.createCSSLink("anchor.css");
		 result += "<div class = \"mainHeader\">"
		  + "<h1 class=\"inlineBlock title\"> Fablix </h1>"
		  + "<div class =\"inlineBlock interaction\">"
		  + "<div>"
		  + "<input type=\"text\" name=\"hero\" id=\"autocomplete\" placeholder=\"Enter Search\"/>" 
		  + "</div>"
		  + " <a class=\"links\" href=\"/Fablix/fablixhome\">Home</a>"
		  + " <a class=\"links\" href=\"/Fablix/advancedSearch\">Advanced Search</a>"
		  + " <a class=\"links\" href=\"/Fablix/checkOut\">Check Out</a>"
		  + "</div>"
		+ "</div>";
		 result += "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js\"></script>";
		 result += "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery.devbridge-autocomplete/1.4.7/jquery.autocomplete.min.js\"></script>";
		 result += "<script src=\"index.js\"></script>";
		 return result;
	}
	
	public static String createAddToCart(String movieID)
	{
		return "<a href=\"/Fablix/checkOut?method=add&movieID=" + movieID + "&qty=1\">"
		+ "<img class=\"addToCart\" src=\"http://www.pngmart.com/files/3/Add-To-Cart-Button-PNG-Pic.png\"/> </a>"
		+ "</div> </div>";
	}
	
	public static String createCheckOutItem(String id, String title, int currentQuantity)
	{
		String result = "<tr>";
		result += "<td>" + title + "</td>";
		result += "<td>$20.00</td>";
		result += "<td><input id=\"" +id+ "\" type=\"text\" value=\"" + currentQuantity + "\"></td>";
		result += "<td><button class=\"" + id + "\" onclick=\"updateClicked(this)\">Update</button><td>";
		result += "<td><button class=\"" + id + "\" onclick=\"removeClicked(this)\">Remove</button><td>";
		result += "</tr>";
		return result;
	}

	public static String createcheckOutTableHeader()
	{
		String result = "<tr>";
		result += "<th>Title</th>";
		result += "<th>Price</th>";
		result += "<th>Quantity</th>";
		result += "<th></th>";
		result += "<th></th>";
		result += "</tr>";
		return result;
	}
	
	public static String createCheckoutAndEmptyButtons()
	{
		String result = "<div class = \"checkoutAndEmptyContainer\">";
		result += "<a href=\"/Fablix/checkingOut\">Continue Checkout</a>";
		result += "<span>   |   </span>";
		result += "<a href=\"/Fablix/checkOut?method=empty&id=null&qty=0\">Empty</a>";		
		result += "</div>";
		return result;
	}
	
	public static String createGenresSearchLink()
	{
		String result = "<div class=\"genreSearch\">";
		search Search = new search();
		try {
			ResultSet res = Search.genreQuery();
			while (res.next())
				result += "<a class=\"spacing\" href=\"/Fablix/searchPage?page=1&method=searchGenre&numPerPage=15&sort=titleA&title=" + res.getString(2) + "\">" + res.getString(2) + "</a>";
			Search.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		result += "</div>";
		return result;
	}
	
	public static String createCheckOutScript()
	{
		return "<script>"
		  + "updateClicked = function (element)"
		  + "{"
		  +  "quantity = document.getElementById(element.getAttribute('class'));"
		  + "quantityValue = quantity.value;"
		  + "location.href = \"/Fablix/checkOut?method=update&id=\" + element.getAttribute('class') + \"&qty=\"  + quantityValue;"
		  + "}\r\n"
		  + "removeClicked = function (element)"
		  + "{"
		  + "location.href = \"/Fablix/checkOut?method=remove&id=\" + element.getAttribute('class') + \"&qty=0\";"
		  + "}"
		+ "</script>";
	}
	
	public static String createTitleSearchLink()
	{
		String result = "<div class=\"titleSearch\">";
		for (int i = 0; i < 10; ++i)
			result += "<a class=\"spacing\" href=\"/Fablix/searchPage?page=1&method=searchTitle&numPerPage=15&sort=titleA&genre=" + i + "\">" + i + "</a>" ;
		for (char character = 'A'; character <= 'Z'; ++character)
			result += "<a class=\"spacing\" href=\"/Fablix/searchPage?page=1&method=searchTitle&numPerPage=15&sort=titleA&genre=" + character + "\">" + character + "</a>";
		result += "</div>";
		return result;
	}
	
	public static String createMovieInfo(String title, Integer year, String director, HashSet<String> actors, HashSet<String> genres,String movieID)
	{
		String result = "";
		result += "<div class=\"movieInfoDiv\">";
		result += "<a class=\"titleStyle\" href=\"/Fablix/movieSinglePage?movieTitle=" + title + "\">" + title + "</a>";
		result += "<h4> Director: " + director + "</h4>";
		result += "<h4> Year: " + year + "</h4>";
		result += "<h4> Movie ID: " + movieID + "</h4>";

		result += "<h4>Genres: ";
		for(String k: genres)
			result +=  k + ", ";
		result = result.substring(0,result.length()-2);
		result += "</h4>";
	
		result += "<p> <b>Actors:</b> ";
		for(String k: actors)
			result += "<a class=\"actorsTag\" href=\"/Fablix/starPage?actor= " + k + "\">" + k + "</a><span> , </span>" ;
		result = result.substring(0,result.length()- "<span> , </span>".length());
		result += "<h4> Price: $20.00 </h4>";
		result += "</p>" + htmlCreator.createAddToCart(movieID);
		result += "</div>";
		return result;
	}
	
	public static String createAdvancedSearchHtml()
	{
		return "<form action=\"/Fablix/searchPage\">"
				+ "<p hidden> page"
				+  "<input type=\"text\" name=\"page\" value=\"1\">"
				+ "</p>"
				+ "<p hidden> method" 
				+ "<input type=\"text\" name=\"method\" value=\"search\">"
				+ "</p>"
				+ "<p hidden> numPerPage"  
				+ "<input type=\"text\" name=\"numPerPage\" value=\"15\">"
				+  "</p hidden>"
				+  "<p hidden> sort"
				+ "<input type=\"text\" name=\"sort\" value=\"titleA\">"
				+  "</p>"
				+ "Title: "
				+ "<input type=\"text\" name=\"title\" value=\"\">"
				+ "<br>"
				+ "<p>Year: " 
				+ "<input type=\"text\" name=\"year\" value=\"\">"
				+  "</p>"
				+  "<p> Director: "
				+ "<input type=\"text\" name=\"director\" value=\"\">"
				+  "</p>"
				+ "<p> Actor Name: "
				+ "<input type=\"text\" name=\"actorName\" value=\"\">"
				+ "</p>"
				+ "<p>Sub String Matching: " 
				+ "<input type=\"checkbox\" name=\"subStringMatching\">"
				+ "</p>"
				+ "<input type=\"submit\" value=\"Submit\">"
				+ "</form>";
	}
	
	public static String createCheckOutForm(String errorMessage)
	{
		String result = "<center><h1>Checking Out</h1>";
		if (errorMessage != null)		
			result += "<h3 style=\"color:red\">" + errorMessage + "</h3>";
		result += "<form action=\"/Fablix/checkingOut\">"
		+  "<h4>First Name: <h4>"
		+  "<input type=\"text\" name=\"firstName\" required/>"
		+  "<h4>Second Name: <h4>"
		+   "<input type=\"text\" name=\"secondName\" required/>"
		+   "<h4>Credit Card: <h4>"
		+   "<input type=\"text\" name=\"secondName\" required/>"
		+   "<h4>Expiration: <h4>"
		+   "<input type=\"text\" name=\"secondName\" required/>"
		+   "<br><input type=\"submit\" value=\"Submit\">"
		+ "</form></center>";
		return result;
	}
	
	
	
	public static String createStarInfo(String name, String id, int birthYear,HashSet<String> titles)
	{
		String birthYearString = Integer.toString(birthYear);
		if(birthYear == 1 || birthYear == 0)
			birthYearString = "Unknown";
		
		String result = "<center>";
		result += "<div class=\"starInfoDiv\">";
		result += "<h1> Name: " + name + "</h1><br>";
		result += "<h3> Star id: " + id + "</h3>";
		result += "<h3> Birth Date: " + birthYearString + "</h3>";
		result += "<h3>Starred in: ";
		for(String k: titles)
			result += "<a class=\"titlesTag\" href=\"/Fablix/movieSinglePage?movieTitle=" + k + "\">" + k + "</a><span> , </span>" ;
		result = result.substring(0,result.length()- "<span> , </span>".length());
		result += "</div></center>";
		return result;
	}
	
	
	public static String createNumPageButton(String queries)
	{
		String result = "<center><div class=\"numPageButton\">";
		queries = queries.replaceAll("page=[0-9]*\\&","page=1&");
		result += "<a href = \"/Fablix/searchPage?" + queries.replaceAll("numPerPage=[0-9]*\\&","numPerPage=5&") + "\">5</a>"; 
		result += "<span> | </span>";
		result += "<a href = \"/Fablix/searchPage?" + queries.replaceAll("numPerPage=[0-9]*\\&","numPerPage=10&") + "\">10</a>"; 
		result += "<span> | </span>";
		result += "<a href = \"/Fablix/searchPage?" + queries.replaceAll("numPerPage=[0-9]*\\&","numPerPage=15&") + "\">15</a>"; 
		result += "<span> | </span>";
		result += "<a href = \"/Fablix/searchPage?" + queries.replaceAll("numPerPage=[0-9]*\\&","numPerPage=20&") + "\">20</a>"; 
		result += "<span> | </span>";
		result += "<a href = \"/Fablix/searchPage?" + queries.replaceAll("numPerPage=[0-9]*\\&","numPerPage=25&") + "\">25</a>"; 
		result += "<span> | </span>";
		result += "</div></center>";
		return result;
		//page=1&method=searchTitle&numPerPage=15&sort=titleA&genre=A
	}
	
	public static String createSortButton(String queries)
	{
		String result = "<center><div class=\"sortButtons\">";
		result += "<a href = \"/Fablix/searchPage?" + queries.replaceAll("sort=.*\\&","sort=titleA&") + "\">TitleA</a>"; 
		result += "<span> | </span>";
		result += "<a href = \"/Fablix/searchPage?" +queries.replaceAll("sort=.*\\&","sort=titleD&") + "\">TitleD</a>"; 
		result += "<span> | </span>";
		result += "<a href = \"/Fablix/searchPage?" +queries.replaceAll("sort=.*\\&","sort=yearA&") + "\">YearA</a>"; 
		result += "<span> | </span>";
		result += "<a href = \"/Fablix/searchPage?" +queries.replaceAll("sort=.*\\&","sort=yearD&") + "\">YearD</a>"; 
		result += "</div></center>";
		return result;
		//page=1&method=searchTitle&numPerPage=15&sort=titleA&genre=A
	}
	
	
	public static String createMetaDataDisplay(String title, ResultSetMetaData data)
	{
		String result = "<center><h3>" + title + "</h3></center>\n";
		result += "<center><table border=1><tr>";
		try {
			for (int i = 1; i <= data.getColumnCount(); ++i)
				result += "<th><pre>" + data.getColumnName(i) + " (  " + data.getColumnTypeName(i) + "[" + data.getColumnType(i) + "]  )</pre></th>";
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		result += "</tr></table></center>\n";
		return result;
	}
	
	
	public static String createStarsInsertForm(int errorMessage)
	{
		String result = "<center><h2>Insert Stars</h2></center>";
		if (errorMessage == 1)
			result += "<center><h2 style=\"color:red;\">Star Already Exist Inside Database</h2></center>";
		else if (errorMessage == 0)
			result += "<center><h2 style=\"color:green;\">Successful Insert Into Database</h2></center>";
		result += "<center><form action=\"/Fablix/_dashboard\" method=\"GET\" >"
				+ "<table>"
				+ "<tr><th>Name</th><th>Year</th><th>Force Into Database</th></tr>"
				+ "<tr><td><input name=\"starName\" type=\"text\" required></input></td>"
				+ "<td><input name=\"birthYear\" type=\"number\"></input></td>"
				+ "<td><center><input name=\"force\" type=\"checkbox\"></input></center></td>"
				+ "</tr></table><center><button style=\"margin-top:1vh;\" type=\"submit\">Update Database</button></center></form></center>";
		return result;
	}
	
	public static String createAddMovieForm(String Message)
	{
		String result = "<center><h2>Add Movie To Database</h2></center>";
		if (!(Message.equals("")))
			result += "<center><h2 style=\"color:red;\">" + Message + "</h2></center>";
		result += "<center><form action=\"/Fablix/_dashboard\" method=\"GET\" >"
				+ "<table>"
				+ "<tr><th>Title</th><th>Movie Year</th><th>Director</th><th>Star Name</th><th>Genre</th></tr>"
				+ "<td><input name=\"movieTitle\" type=\"text\" required></input></td>"
				+ "<td><input name=\"movieYear\" type=\"number\" required></input></td>"
				+ "<td><input name=\"movieDirector\" type=\"text\" required></input></td>"
				+ "<td><input name=\"movieStar\" type=\"text\" required></input></td>"
				+ "<td><input name=\"movieGenre\" type=\"text\" required></input></td>"
				+ "</tr></table><center><button style=\"margin-top:1vh;\" type=\"submit\">Update Database</button></center></form></center>";
		return result;
	}
	
	
}
