package agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import misc.Recommendation;

public class Advisor {

	private static final int USER_ID = 1;

	private static final String DB_URL = "jdbc:mysql://localhost/movie";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "12345";
	private static Connection dbConnection;

	private static ArrayList<Recommendation> recommendations;
	private static HashSet<Integer> watchedMovies;
	private static HashMap<Integer,Double> movieAverages;
	public static ArrayList<Recommendation> execute() {
		recommendations = new ArrayList<Recommendation>();
		watchedMovies = new HashSet<Integer>();
		movieAverages = new HashMap<Integer, Double>();
		try {
			if (initDB()) {
				int selectedGenre = getGenreToWatch();
				generateAverageRatingsOfMovies(selectedGenre);
				System.out.println("b");
				populateWatchedMovies(selectedGenre);
				getRecommendationFromFriend(selectedGenre); //Get friends recommendations
				removeWatchedMoviesFromRecommendations(); // remove watched movies from recommendations
				if (recommendations.size()<5) { // if recommended movies are less than 5 after removing watched movies
					System.out.println(selectedGenre);
					getRecommendationFromFriendsOfFriends(selectedGenre);
					getProfessionalRecommendation(selectedGenre);
				}
				removeWatchedMoviesFromRecommendations();
				Collections.sort(recommendations);
				return recommendations;
				// Line to set recommended movie fields
				// TODO: update quality and prefs in planner(preferably) or
				// runner, not here
				// update trust value of the random guy if his movie is
				// watched.
			} else {
				System.out.println("Error in DB connection");
			}
			dbConnection.close();
		} catch (SQLException sql) {
			//System.out.println("Error in DB connection in methods");
		}
		return null;
	}

	public static ArrayList<Recommendation> getRecommendations(){
		return recommendations;
	}
	
	public static HashMap<Integer,Double> getMovieAverages(){
		return movieAverages;
	}

	private static boolean initDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			dbConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	// Return the genre the user liked the most. Compute it via finding
	// the highest averaged genre among the films that was rated higher than 3.5
	private static int getGenreToWatch() throws SQLException {
		Statement stmt = dbConnection.createStatement();
		String averagesQuery = "SELECT GENREID,COUNT(MOVIERATING) FROM "
				+ "MOVIE_RATINGS WHERE MOVIERATING > 3.5  AND USERID= " + USER_ID + " GROUP BY GENREID;";

		double[] weights = new double[17];
		Arrays.fill(weights, 1);
		double weightSum = 17;
		ResultSet rs = stmt.executeQuery(averagesQuery);
		while (rs.next()) {
			int genreID = (int) rs.getDouble("GENREID");
			double avgRating = rs.getDouble("COUNT(MOVIERATING)");
			weights[genreID - 1] = avgRating + 1;
			weightSum = weightSum + avgRating;
			System.out.println("Genre: " + genreID + " Rating: " + weights[genreID - 1]);
		}
		// System.out.println(weightSum);
		double result = Math.random() * weightSum;
		double runningSum = 0;
		for (int i = 0; i < weights.length; i++) {
			if (result < runningSum) {
				rs.close();
				stmt.close();
				return i;
			}
			runningSum += weights[i];
		}
		rs.close();
		stmt.close();
		return -1;
	}

	private static void populateWatchedMovies(int selectedGenre) {
		try {
			Statement stmt = dbConnection.createStatement();
			String watchedMoviesQuery = "SELECT MOVIEID FROM movie_ratings " + "WHERE userID = " + USER_ID
					+ " AND genreID = " + selectedGenre + ";";
			ResultSet rs = stmt.executeQuery(watchedMoviesQuery);
			while (rs.next()) {
				watchedMovies.add(rs.getInt("movieid"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in db connection of watched movie population");
			e.printStackTrace();
		}

	}

	// Returns movie ID and movie rate for
	private static void getRecommendationFromFriend(int selectedGenre) throws SQLException {
		Statement stmt = dbConnection.createStatement();
		// No need to ortder by since they will be ordered later
		String friendRecommendationQuery = "SELECT movie_ratings.movieID, movie_ratings.userID, movie_ratings.movieRating, trust.trustValue, movie_ratings.movieRating*trust.trustValue as Watchability "
				+ "FROM trust INNER JOIN movie_ratings ON movie_ratings.userID = trust.trusteeID "
				+ "WHERE trust.trustorID = " + USER_ID + " AND movie_ratings.genreID = " + selectedGenre + ";";

		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		while (rs.next()) {
			int movieID = rs.getInt("movie_ratings.movieID");
			int userID = rs.getInt("movie_ratings.userID");
			double movieRating = rs.getDouble("movie_ratings.movieRating");
			double userTrust = rs.getDouble("trust.trustValue");
			double watchability = rs.getDouble("watchability") + (1 - userTrust)*movieAverages.get(movieID);
			Recommendation recommendation = new Recommendation( movieID , userID, movieRating, userTrust, watchability);
			recommendations.add(recommendation);
		}
		rs.close();
		stmt.close();
	}

	private static void getRecommendationFromFriendsOfFriends(int genre) throws SQLException {
		Statement stmt = dbConnection.createStatement();
		// No need for order by since they will be ordered later.
		String friendRecommendationQuery = "SELECT moviesbygenre.movieID, moviesbygenre.userID, moviesbygenre.movieRating, friendsfriend.trustValue, moviesbygenre.movieRating*friendsfriend.trustValue as Watchability "
				+ "FROM (SELECT friends.trusteeID as trusteeID, me.trustValue*friends.trustValue as trustValue "
				+ "	   FROM (SELECT * FROM trust WHERE trust.trustorID = " + USER_ID + ") as me "
				+ "		     INNER JOIN " + "            trust as friends "
				+ "	   WHERE me.trusteeID = friends.trustorID) " + "	   AS friendsfriend " + "	   INNER JOIN "
				+ "	  (SELECT * " + "      FROM movie_ratings " + "      WHERE movie_ratings.genreID = " + genre + ") "
				+ "      AS moviesbygenre " + "ON moviesbygenre.userID = friendsfriend.trusteeID; ";
		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		while (rs.next()) {
			int movieID = rs.getInt("moviesbygenre.movieID");
			int userID = rs.getInt("moviesbygenre.userID");
			double movieRating = rs.getDouble("moviesbygenre.movieRating");
			double userTrust = rs.getDouble("friendsfriend.trustValue");
			double watchability = rs.getDouble("watchability") + (1 - userTrust)*movieAverages.get(movieID);
			Recommendation recommendation = new Recommendation( movieID , userID, movieRating, userTrust, watchability);
			recommendations.add(recommendation);
		}
		rs.close();
		stmt.close();
	}

	private static int getGenreProfessional(int genre) {
		int genreProfessional = -1;
		try {
			Statement stmt = dbConnection.createStatement();
			String genreProfessionalSearchQuery = "SELECT userID, COUNT(*) AS MovieRateAmount " + "FROM movie_ratings "
					+ "WHERE genreID = " + genre + " AND NOT userID IN (SELECT trusteeID FROM trust WHERE trustorID = "
					+ USER_ID + ") " + "GROUP BY userID ORDER BY MovieRateAmount DESC " + "LIMIT 1;";
			ResultSet rs = stmt.executeQuery(genreProfessionalSearchQuery);
			rs.next();
			genreProfessional = (int) rs.getInt("userID");
			rs.close();
			stmt.close();

		} catch (SQLException e) {
			System.out.println("Error in db connection of getGenrePro");
			e.printStackTrace();
		}
		return genreProfessional;
	}

	private static void getProfessionalRecommendation(int genre) {
		int genreProfessional = getGenreProfessional(genre);

		try {
			Statement stmt = dbConnection.createStatement();
			String professionalRecommendationQuery = "SELECT movie_ratings.movieID, movie_ratings.movieRating "
					+ "FROM movie_ratings " + "WHERE userID =" + genreProfessional + " AND genreID =" + genre + " "
					+ "ORDER BY movieRating DESC;";

			ResultSet rs;
			rs = stmt.executeQuery(professionalRecommendationQuery);
			while (rs.next()) {
				int movieID = rs.getInt("movie_ratings.movieID");
				double movieRating = rs.getDouble("movie_ratings.movieRating");
				double watchability = movieRating * 0.5 + movieAverages.get(movieID) * 0.5;
				Recommendation recommendation = new Recommendation(rs.getInt("movie_ratings.movieID"),
						genreProfessional, movieRating, 0.5, watchability);
				recommendations.add(recommendation);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in db connection of professional");
			e.printStackTrace();
		}

	}

	private static void generateAverageRatingsOfMovies(int selectedGenre) throws SQLException {
		try {
			Statement stmt = dbConnection.createStatement();
			String movieAverageQuery = "SELECT movieID, AVG(movieRating) as averageRating FROM movie_ratings WHERE genreID="+selectedGenre+" GROUP BY movieID;";
			ResultSet rs = stmt.executeQuery(movieAverageQuery);
			while(rs.next()) {
				movieAverages.put(rs.getInt("movieID"), rs.getDouble("averageRating"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in db connection of professional");
			e.printStackTrace();
		}
	}

	private static boolean areAllRecommendedMoviesAreWatched() {
		for (Recommendation recommendation : recommendations) {
			if (!watchedMovies.contains(recommendation.movieID)) {
				return false;
			}
		}
		return true;
	}
	
	private static void removeWatchedMoviesFromRecommendations() {
		for (int i = 0; i < recommendations.size(); i++) {
			if (watchedMovies.contains(recommendations.get(i).movieID)) {
				recommendations.remove(i);
				i--;
			}
		}
	}
}
