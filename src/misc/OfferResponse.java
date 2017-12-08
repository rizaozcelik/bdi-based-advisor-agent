package misc;

public class OfferResponse {

	private ResponseType type;
	private double offersUtility;

	public OfferResponse(ResponseType type, double offersUtility) {
		this.type = type;
		this.offersUtility = offersUtility;
	}

	public ResponseType getType() {
		return type;
	}

	public void setType(ResponseType type) {
		this.type = type;
	}

	public double getOffersUtility() {
		return offersUtility;
	}

	public void setOffersUtility(double offersUtility) {
		this.offersUtility = offersUtility;
	}
}
