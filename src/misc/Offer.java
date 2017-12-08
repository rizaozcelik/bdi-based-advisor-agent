package misc;

public class Offer {
	private int owner;
	private double ownerUtility;
	private int typeID; // The id of the genre or the movie
	
	public Offer(int owner, int typeID, double ownerUtility) { //ERASED RESPONSE SHOULDN'T NEED IT.?
		this.owner = owner;
		this.ownerUtility = ownerUtility;
		this.typeID = typeID;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public double getOwnerUtility() {
		return ownerUtility;
	}

	public void setOwnerUtility(double ownerUtility) {
		this.ownerUtility = ownerUtility;
	}

	public int getTypeID() {
		return typeID;
	}

	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}

}
