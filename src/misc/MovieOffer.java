package misc;

public class MovieOffer {
	private int owner, genre, movieID, ownerUtility;
	private OfferResponse response;
	
	public MovieOffer(int owner, int genre, int movieID, int ownerUtility, OfferResponse response) {
		this.owner = owner;
		this.genre = genre;
		this.movieID = movieID;
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

	public int getGenre() {
		return genre;
	}

	public void setGenre(int genre) {
		this.genre = genre;
	}

	public int getMovieID() {
		return movieID;
	}

	public void setMovieID(int movieID) {
		this.movieID = movieID;
	}

	public int getOwnerUtility() {
		return ownerUtility;
	}

	public void setOwnerUtility(int ownerUtility) {
		this.ownerUtility = ownerUtility;
	}
	
}
