package agents;

import java.util.ArrayList;

import misc.Offer;
import misc.OfferResponse;

public class GenreNegotiator extends Negotiator {
	private ArrayList<Integer> genres;

	public GenreNegotiator(int agentID, int acceptanceParameter, ArrayList<Integer> genres) {
		super(agentID, acceptanceParameter);
		this.genres = genres;
	}

	@Override
	public OfferResponse evaluate(Offer receivedOffer) {
		if (receivedOffer == null) {
			// this is the first round.
			return null;
		}
		return null;
	}

	@Override
	public Offer proposeOffer() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public double computePersonalUtility(int typeID) {
		return 0;
	}

}
