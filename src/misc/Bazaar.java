package misc;

import java.util.ArrayList;

import agents.Negotiator;

public class Bazaar {

	private boolean forceAgreement;
	private ArrayList<Negotiator> negotiators;
	private double maximumTotalUtility;

	public Bazaar(boolean forceAgreement, ArrayList<Negotiator> negotiators) {
		this.forceAgreement = forceAgreement;
		this.negotiators = negotiators;
		maximumTotalUtility = 0;
	}

	public int runNegotiation() {
		int numberOfNegotiators = negotiators.size();
		Offer currentOffer = null;
		Offer bestOffer = null;
		int round = 0;
		boolean accepted = false;
		while (!accepted && round < 6 * numberOfNegotiators) {
			double totalUtility = 0;
			// Proposer already accepts
			int acceptanceCount = 1;
			Negotiator agentToProposeOffer = negotiators.get(round % 6);
			currentOffer = agentToProposeOffer.proposeOffer();
			for (int i = 0; i < numberOfNegotiators; i++) {
				if (i != round % 6) {
					OfferResponse response = negotiators.get(i).evaluate(currentOffer);
					if (response.getType() == ResponseType.Accept) {
						acceptanceCount++;
					}
					totalUtility += response.getOffersUtility();
				} else {
					// Otherwise this the agent that proposes the offer
					totalUtility += currentOffer.getOwnerUtility();
				}
			}

			accepted = acceptanceCount >= numberOfNegotiators / 2;
			if (totalUtility > maximumTotalUtility) {
				maximumTotalUtility = totalUtility;
				bestOffer = currentOffer;
			}
			round++;
		}

		if (currentOffer.getResponse() == ResponseType.Accept) {
			return currentOffer.getTypeID();
		} else if (forceAgreement) {
			return bestOffer.getTypeID();
		}
		System.out.println("No agreement has been established");
		return -1;
	}

}
