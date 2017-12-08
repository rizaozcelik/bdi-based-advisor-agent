package misc;

import agents.Negotiator;

public class Bazaar {

	private boolean forceAgreement;
	private Negotiator neg1, neg2;
	private double maximumTotalUtility;

	public Bazaar(boolean forceAgreement, Negotiator neg1, Negotiator neg2) {
		this.forceAgreement = forceAgreement;
		this.neg1 = neg1;
		this.neg2 = neg2;
		maximumTotalUtility = 0;
	}

	public int runNegotiation() {
		Offer currentOffer = null;
		Offer bestOffer = null;
		int round = 1;
		while (currentOffer.getResponse() != OfferResponse.Accept && round < 13) {
			double totalUtility;
			if (round % 2 == 0) {
				currentOffer = neg1.evaluate(currentOffer);
				totalUtility = currentOffer.getOwnerUtility() + neg1.computePersonalUtility(currentOffer.getTypeID());
			} else {
				currentOffer = neg2.evaluate(currentOffer);
				totalUtility = currentOffer.getOwnerUtility() + neg2.computePersonalUtility(currentOffer.getTypeID());
			}
			if (totalUtility > maximumTotalUtility) {
				maximumTotalUtility = totalUtility;
				bestOffer = currentOffer;
			}
			round++;
		}

		if (currentOffer.getResponse() == OfferResponse.Accept) {
			return currentOffer.getTypeID();
		} else if (forceAgreement) {
			return bestOffer.getTypeID();
		}
		System.out.println("No agreement has been established");
		return -1;
	}

}
