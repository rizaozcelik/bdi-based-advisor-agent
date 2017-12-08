package agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;

import java.sql.Statement;

import misc.Offer;
import misc.Recommendation;

public class MovieNegotiator extends Negotiator {
	private ArrayList<Recommendation> recommendations;

	public MovieNegotiator(int agentID, int acceptanceParameter, ArrayList<Recommendation> recommendations) {
		super(agentID, acceptanceParameter);
		this.recommendations = recommendations;
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
		final String DB_URL = "jdbc:mysql://localhost/movie";
		final String DB_USER = "root";
		final String DB_PASSWORD = "12345";
		Connection dbConnection = null;

		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		} catch (Exception e) {
			System.out.println("Error in db connection opening in negotiator");
		}

		if (dbConnection != null) {
			try {
				Statement stmt = dbConnection.createStatement();
				String friendsRatingQuery = "SELECT movie_ratings.movieRating, trust.trustValue FROM movie_ratings "
						+ " INNER JOIN trust ON movie_ratings.userID = trust.trusteeID WHERE movieID = " + typeID
						+ " AND trustorID = " + agentID + " ORDER BY movieRating * trustValue DESC LIMIT 1;";
				ResultSet rs = stmt.executeQuery(friendsRatingQuery);
				int friendsRating = 0;
				double trustValue = 0, averageRating = 0;
				while (rs.next()) {
					friendsRating = (int) rs.getInt("movieRating");
					trustValue = rs.getDouble("trustValue");
				}
				System.out.println("balfaldfla");
				String getMovieAvgQuery = "SELECT AVG(movieRating) AS average FROM movie_ratings WHERE movieID = "
						+ typeID;
				rs = stmt.executeQuery(getMovieAvgQuery);
				while (rs.next()) {
					averageRating = rs.getDouble("average");
				}
				System.out.println(trustValue + " " + friendsRating + " " + averageRating);
				return trustValue * friendsRating + (1 - trustValue) * averageRating;
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in db querying");
			}
		}
		return 0;
	}

}
