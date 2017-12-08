package agents;

import misc.Offer;
import misc.OfferResponse;

public abstract class Negotiator {
	protected int agentID, acceptanceParameter;

	public Negotiator(int agentID, int acceptanceParameter) {
		this.agentID = agentID;
		this.acceptanceParameter = acceptanceParameter;
	}

	abstract public OfferResponse evaluate(Offer receivedOffer);

	abstract public Offer proposeOffer();

	abstract public double computePersonalUtility(int typeID);

}
