package misc;

public class MovieEvent extends Event{
	int recommendationNumber;
	int movieID;
	public MovieEvent(int iD, String type, int date, String time, int quality, boolean isObliged, int recommendationNumber, int movieID) {
		super(iD, type, date, time, quality, isObliged);
		this.recommendationNumber = recommendationNumber;
		this.movieID = movieID;
		// TODO Auto-generated constructor stub
	}
	public int getRecommendationNumber() {
		return recommendationNumber;
	}
	@Override
	public String toString() {
		//+(date+16) + "\t"
		return type + "\t" + startTime + "-" + endTime + "\t" + movieID;
	}
}
