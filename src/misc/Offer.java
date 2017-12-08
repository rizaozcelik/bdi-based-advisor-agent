package misc;

public class Offer {
	private int owner, typeID, ownerUtility;
	private OfferResponse response;

	public Offer(int owner, int genre, int typeID, int ownerUtility, OfferResponse response) {
		this.owner = owner;
		this.typeID = typeID;
		this.ownerUtility = ownerUtility;
		this.response = response;
	}

	public OfferResponse getResponse() {
		return response;
	}

	public void setResponse(OfferResponse response) {
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
