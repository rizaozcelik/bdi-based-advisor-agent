package agents;

import java.util.ArrayList;

import misc.Offer;
import misc.OfferResponse;
import misc.ResponseType;
import sun.security.acl.OwnerImpl;

public class GenreNegotiator extends Negotiator {
	private ArrayList<Integer> genres; //Genres are ordered by their preferences
	private int numberOfProposes;

	public GenreNegotiator(int agentID, int acceptanceParameter, ArrayList<Integer> genres) {
		super(agentID, acceptanceParameter);
		this.genres = genres; //Genre arraylist should be ordered wrt preferences.
		numberOfProposes = 0;
	}

	@Override
	public OfferResponse evaluate(Offer receivedOffer) {
		if (receivedOffer == null) {
			// this is the first round.
			return null;
		}
		// dummy line
		if (genres == null)
			return null;
				
		int genreID = receivedOffer.getTypeID();
		int userUtility = genres.size();
		for(int i=0; i<genres.size(); i++) {
			if(genres.get(i) == genreID) userUtility = i;
		}
		OfferResponse response;
		if(userUtility<acceptanceParameter) { //UTILITY OF THE GENRE IS THE TOTAL NUMBER OF GENRES - GENRES INDEX
			response = new OfferResponse(ResponseType.Accept, (double)(genres.size()-userUtility));
		}else {
			response = new OfferResponse(ResponseType.Accept, (double)(genres.size()-userUtility));
		}
		return response;
	}

	@Override
	public Offer proposeOffer() { // Offers utility is the total number of genres - the index of the genre (numberOfProposes)
		// TODO Auto-generated method stub
		int genreID = genres.get(numberOfProposes);
		Offer offer = new Offer(agentID, genreID, (double) genres.size() - numberOfProposes);
		numberOfProposes++;
		return offer;
	}

	@Override
	public double computePersonalUtility(int genreID) {
		return 0;
	}

}
