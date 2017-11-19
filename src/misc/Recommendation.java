package misc;

public class Recommendation implements Comparable<Recommendation> {
	public int movieID, userID;
	public double movieRating, trustValue, watchability, averageRating, quality;

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
	@Override
	public String toString() {
		//+(date+16) + "\t"
		return movieID + "\t" + movieRating + "\t" + userID + "\t" + trustValue;
	}

}
