package misc;

public class Recommendation implements Comparable<Recommendation> {
	public int movieID, userID;
	public double movieRating, trustValue, watchability;

	public Recommendation(int movieID, int userID, double movieRating, double trustValue, double watchability) {
		this.movieID = movieID;
		this.userID = userID;
		this.movieRating = movieRating;
		this.trustValue = trustValue;
		this.watchability = watchability;
	}
	@Override
	public int compareTo(Recommendation r) {
		return Double.compare(watchability, r.watchability);
	}

}
