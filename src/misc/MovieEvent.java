package misc;

public class MovieEvent extends Event{
	int recommendationNumber;
	
	public MovieEvent(int iD, String type, int date, String time, int quality, boolean isObliged, int recommendationNumber) {
		super(iD, type, date, time, quality, isObliged);
		this.recommendationNumber = recommendationNumber;
		// TODO Auto-generated constructor stub
	}
	public int getRecommendationNumber() {
		return recommendationNumber;
	}
	@Override
	public String toString() {
		//+(date+16) + "\t"
		return type +" " + recommendationNumber+ "\t" + startTime + "-" + endTime + "\t" ;
	}
}
