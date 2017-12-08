package agents;

import java.util.ArrayList;

import misc.Offer;


public class GenreNegotiator extends Negotiator {
	private ArrayList<Integer> genres;

	public GenreNegotiator(int agentID, int acceptanceParameter, ArrayList<Integer> genres) {
		super(agentID, acceptanceParameter);
		this.genres = genres;
	}

	@Override
	public Offer evaluate(Offer receivedOffer) {
		if (receivedOffer == null) {
			// this is the first round.
			return rejectOffer();
		}
		computePersonalUtility(receivedOffer.getTypeID());
		return acceptOffer();
	}

	@Override
	Offer acceptOffer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	Offer rejectOffer() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public double computePersonalUtility(int typeID) {
		return 0;
	}

}
