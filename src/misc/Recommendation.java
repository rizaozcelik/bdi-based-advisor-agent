package misc;

public class Recommendation {
	public int movieID, userID;
	public double movieRating, trustValue;
	public Recommendation(int movieID, int userID, double movieRating, double trustValue) {
		this.movieID = movieID;
		this.userID = userID;
		this.movieRating = movieRating;
		this.trustValue = trustValue;
	}
	
}
