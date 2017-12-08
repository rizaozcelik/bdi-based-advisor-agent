package agents;


import misc.Offer;

public abstract class Negotiator {
	protected int agentID, acceptanceParameter;

	public Negotiator(int agentID, int acceptanceParameter) {
		this.agentID = agentID;
		this.acceptanceParameter = acceptanceParameter;
	}
	abstract public Offer evaluate(Offer receivedOffer);
	abstract Offer acceptOffer();
	abstract Offer rejectOffer();
	abstract public double computePersonalUtility(int typeID);
		
}
