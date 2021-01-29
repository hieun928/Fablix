package server;
import java.util.HashSet;

public class movieData 
{
   public String movieID = "";
   public int year = 0;
   public String director = "";
   public double rating = 0.0;
   public HashSet<String> genres = new HashSet<String>();
   public HashSet<String> actors = new HashSet<String>();
}
