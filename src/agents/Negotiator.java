package agents;

import misc.Offer;
import misc.OfferResponse;

public abstract class Negotiator {
	protected int agentID, acceptanceParameter;
	protected boolean increaseAcceptenceOverTime;
	public Negotiator(int agentID, int acceptanceParameter, boolean increaseAcceptenceOverTime) {
		this.agentID = agentID;
		this.acceptanceParameter = acceptanceParameter;
		this.increaseAcceptenceOverTime = increaseAcceptenceOverTime;
	}

	abstract public OfferResponse evaluate(Offer receivedOffer);

	abstract public Offer proposeOffer();

	abstract public double computePersonalUtility(int typeID);

}
