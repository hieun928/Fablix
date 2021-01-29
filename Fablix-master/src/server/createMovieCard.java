package server;

public class createMovieCard {
	public static String createCard(String movieTitle,int year,double rating, String movieID)
	{
		String result = "<div class = \"card\">"
				+ "<h3><a class=\"titleStyle\" href=\"/Fablix/movieSinglePage?movieTitle=" + movieTitle + "\">" + movieTitle + "</a></h3>"
				+ "<div class = \"subInfo\"> <h4>Year: " + year + "</h4>"
				+ "<h4> Rating: " + rating + "</h4> "
				+ "<h4> Price: $20.00 </h4>"
				+ "<a href=\"/Fablix/checkOut?method=add&movieID=" + movieID + "&qty=1\">"
				+ "<img class=\"addToCart\" src=\"http://www.pngmart.com/files/3/Add-To-Cart-Button-PNG-Pic.png\"/> </a>"
				+ "</div> </div>";
		return result;
	}
}
