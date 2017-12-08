package misc;

public class Offer {
	private int owner, typeID, ownerUtility;
	private ResponseType response;

	public Offer(int owner, int genre, int typeID, int ownerUtility, ResponseType response) {
		this.owner = owner;
		this.typeID = typeID;
		this.ownerUtility = ownerUtility;
		this.response = response;
	}

	public ResponseType getResponse() {
		return response;
	}

	public void setResponse(ResponseType response) {
		this.response = response;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public int getTypeID() {
		return typeID;
	}

	public void setMovieID(int typeID) {
		this.typeID = typeID;
	}

	public int getOwnerUtility() {
		return ownerUtility;
	}

	public void setOwnerUtility(int ownerUtility) {
		this.ownerUtility = ownerUtility;
	}

}
