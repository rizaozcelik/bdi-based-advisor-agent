package misc;

public class Event implements Comparable<Event> {
	public int ID, date, startTime, endTime, quality;
	public String type;
	public boolean isObliged;

	public Event(int iD, String type, int date, String time, int quality, boolean isObliged) {
		this.ID = iD;
		this.type = type;
		this.date = date;
		this.startTime = Integer.parseInt(time.split("-")[0]);
		this.endTime = Integer.parseInt(time.split("-")[1]);
		this.quality = quality;
		this.isObliged = isObliged;
	}

	@Override
	public String toString() {
		//+(date+16) + "\t"
		return type + "\t" + startTime + "-" + endTime + "\t";
	}
	@Override
	public int compareTo(Event e){
		return this.endTime - e.endTime;
	}
}
