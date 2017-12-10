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

	public Offer runNegotiation() {
		int numberOfNegotiators = negotiators.size();
		Offer currentOffer = null;
		Offer bestOffer = null;
		int round = 0;
		boolean accepted = false;
		while (!accepted && round < 6 * numberOfNegotiators) {
			System.out.println("\nRound: "+ round);
			double totalUtility = 0;
			// Proposer already accepts
			int acceptanceCount = 1;
			Negotiator agentToProposeOffer = negotiators.get(round % negotiators.size());
			currentOffer = agentToProposeOffer.proposeOffer();
			for (int i = 0; i < numberOfNegotiators; i++) {
				if (i != round % negotiators.size()) {
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
			//SHOULD CHANGE AT LEAST NEEDS TO BE HALF + 1. 
			accepted = acceptanceCount > numberOfNegotiators / 2;
			if (totalUtility > maximumTotalUtility) {
				maximumTotalUtility = totalUtility;
				bestOffer = currentOffer;
			}
			round++;
		}
		System.out.println();
		if (accepted) {
			System.out.println("Negotiators reached an agreement on the offer of Agent: "+ currentOffer.getOwner());
			System.out.println();
			return currentOffer;
		} else if (forceAgreement) {
			System.out.println("Negotiators could not agree on an offer. The offer "+ bestOffer.getTypeID()+" is selected since it has the maximum total utility.");
			System.out.println();
			return bestOffer;
		}
		System.out.println("No agreement has been established");
		System.out.println();

		return null;
	}

}
