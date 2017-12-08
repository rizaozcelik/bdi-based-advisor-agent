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

public class MovieAdvisor {

	private static final String DB_URL = "jdbc:mysql://localhost/movie";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "12345";
	private static Connection dbConnection;

	private int userID, selectedGenre, preDeterminedGenre;
	private ArrayList<Recommendation> recommendations;
	private HashSet<Integer> watchedMovies;
	private HashMap<Integer, Double> movieAverages;

	public MovieAdvisor(int userID) {
		this.userID = userID;
		preDeterminedGenre = -1;
		this.recommendations = new ArrayList<Recommendation>();
		this.watchedMovies = new HashSet<Integer>();
		this.movieAverages = new HashMap<Integer, Double>();
	}

	public MovieAdvisor(int userID, int preDeterminedGenre) {
		this.userID = userID;
		this.preDeterminedGenre = preDeterminedGenre;
		this.recommendations = new ArrayList<Recommendation>();
		this.watchedMovies = new HashSet<Integer>();
		this.movieAverages = new HashMap<Integer, Double>();
	}

	public ArrayList<Recommendation> execute() {
		try {
			if (initDB()) {
				if (preDeterminedGenre == -1) {
					selectedGenre = getGenreToWatch();
				} else {
					selectedGenre = preDeterminedGenre;
				}
				// Demo purposes
				selectedGenre = 14;
				generateAverageRatingsOfMovies(selectedGenre);
				populateWatchedMovies(selectedGenre);
				System.out.println("Going to ask your friends for movie recommendations.");
				// Get friends recommendations
				getRecommendationFromFriend(selectedGenre);
				// remove watched movies from recommendations
				removeWatchedMoviesFromRecommendations();
				// if recommended movies are less than 5 after removing watched
				// movies
				if (recommendations.size() < 6) {
					System.out.println(
							"Your friends didn't recommend enough movies. Going to ask your friends of friends and to the genre expert. ");
					getRecommendationFromFriendsOfFriends(selectedGenre);
					getProfessionalRecommendation(selectedGenre);
				}
				System.out.println();
				removeWatchedMoviesFromRecommendations();
				Collections.sort(recommendations);
				userLog();
				return new ArrayList<Recommendation>(recommendations.subList(0, 6));
			} else {
				System.out.println("Error in DB connection");
			}
			dbConnection.close();
		} catch (SQLException sql) {
			System.out.println("Error in DB connection in methods");
		}
		return null;
	}

	public ArrayList<Recommendation> getRecommendations() {
		return recommendations;
	}

	public HashMap<Integer, Double> getMovieAverages() {
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
	private int getGenreToWatch() throws SQLException {
		Statement stmt = dbConnection.createStatement();
		String averagesQuery = "SELECT GENREID,COUNT(MOVIERATING) FROM "
				+ "MOVIE_RATINGS WHERE MOVIERATING > 3.5  AND USERID= " + userID + " GROUP BY GENREID;";

		int[] weights = new int[17];
		Arrays.fill(weights, 1);
		double weightSum = 17;
		ResultSet rs = stmt.executeQuery(averagesQuery);
		System.out.println("The number of movies the user " + userID + " watched, grouped by their genres:");
		while (rs.next()) {
			int genreID = (int) rs.getDouble("GENREID");
			int numOfRatings = rs.getInt("COUNT(MOVIERATING)");
			weights[genreID - 1] = numOfRatings + 1;
			weightSum = weightSum + numOfRatings;
			System.out.println("Genre: " + genreID + "\tNumOfMovies: " + (weights[genreID - 1] - 1));
		}
		System.out.println();
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

	private void populateWatchedMovies(int selectedGenre) {
		try {
			Statement stmt = dbConnection.createStatement();
			String watchedMoviesQuery = "SELECT MOVIEID FROM movie_ratings " + "WHERE userID = " + userID
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
	private void getRecommendationFromFriend(int selectedGenre) throws SQLException {
		Statement stmt = dbConnection.createStatement();
		// No need to ortder by since they will be ordered later
		String friendRecommendationQuery = "SELECT movie_ratings.movieID, movie_ratings.userID, movie_ratings.movieRating, trust.trustValue, movie_ratings.movieRating*trust.trustValue as Watchability "
				+ "FROM trust INNER JOIN movie_ratings ON movie_ratings.userID = trust.trusteeID "
				+ "WHERE trust.trustorID = " + userID + " AND trust.trustValue>0.5 AND movie_ratings.genreID = "
				+ selectedGenre + ";";

		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		while (rs.next()) {
			int movieID = rs.getInt("movie_ratings.movieID");
			int userID = rs.getInt("movie_ratings.userID");
			double movieRating = rs.getDouble("movie_ratings.movieRating");
			double userTrust = rs.getDouble("trust.trustValue");
			double watchability = rs.getDouble("watchability") + (1 - userTrust) * movieAverages.get(movieID);
			Recommendation recommendation = new Recommendation(movieID, userID, movieRating, userTrust, watchability);
			recommendations.add(recommendation);
		}
		rs.close();
		stmt.close();
	}

	private void getRecommendationFromFriendsOfFriends(int genre) throws SQLException {
		Statement stmt = dbConnection.createStatement();
		// No need for order by since they will be ordered later.
		String friendRecommendationQuery = "SELECT moviesbygenre.movieID, moviesbygenre.userID, moviesbygenre.movieRating, friendsfriend.trustValue, moviesbygenre.movieRating*friendsfriend.trustValue as Watchability "
				+ "FROM (SELECT friends.trusteeID as trusteeID, me.trustValue*friends.trustValue as trustValue "
				+ "	   FROM (SELECT * FROM trust WHERE trust.trustorID = " + userID + " AND trustValue > 0.5) as me "
				+ "		     INNER JOIN " + "            trust as friends "
				+ "	   WHERE me.trusteeID = friends.trustorID AND friends.trustValue>0.5) " + "	   AS friendsfriend "
				+ "	   INNER JOIN " + "	  (SELECT * " + "      FROM movie_ratings "
				+ "      WHERE movie_ratings.genreID = " + genre + ") " + "      AS moviesbygenre "
				+ "ON moviesbygenre.userID = friendsfriend.trusteeID; ";
		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		while (rs.next()) {
			int movieID = rs.getInt("moviesbygenre.movieID");
			int userID = rs.getInt("moviesbygenre.userID");
			double movieRating = rs.getDouble("moviesbygenre.movieRating");
			double userTrust = rs.getDouble("friendsfriend.trustValue");
			double watchability = rs.getDouble("watchability") + (1 - userTrust) * movieAverages.get(movieID);
			Recommendation recommendation = new Recommendation(movieID, userID, movieRating, userTrust, watchability);
			recommendations.add(recommendation);
		}
		rs.close();
		stmt.close();
	}

	private int getGenreProfessional(int genre) {
		int genreProfessional = -1;
		try {
			Statement stmt = dbConnection.createStatement();
			String genreProfessionalSearchQuery = "SELECT userID, COUNT(*) AS MovieRateAmount " + "FROM movie_ratings "
					+ "WHERE genreID = " + genre + " AND NOT userID IN (SELECT trusteeID FROM trust WHERE trustorID = "
					+ userID + ") " + "GROUP BY userID ORDER BY MovieRateAmount DESC " + "LIMIT 1;";
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

	private void getProfessionalRecommendation(int genre) {
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

	private void generateAverageRatingsOfMovies(int selectedGenre) throws SQLException {
		try {
			Statement stmt = dbConnection.createStatement();
			String movieAverageQuery = "SELECT movieID, AVG(movieRating) as averageRating FROM movie_ratings WHERE genreID="
					+ selectedGenre + " GROUP BY movieID;";
			ResultSet rs = stmt.executeQuery(movieAverageQuery);
			while (rs.next()) {
				movieAverages.put(rs.getInt("movieID"), rs.getDouble("averageRating"));
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in db connection of professional");
			e.printStackTrace();
		}
	}

	// private boolean areAllRecommendedMoviesAreWatched() {
	// for (Recommendation recommendation : recommendations) {
	// if (!watchedMovies.contains(recommendation.movieID)) {
	// return false;
	// }
	// }
	// return true;
	// }

	private void removeWatchedMoviesFromRecommendations() {
		for (int i = 0; i < recommendations.size(); i++) {
			if (watchedMovies.contains(recommendations.get(i).movieID)) {
				recommendations.remove(i);
				i--;
			}
		}
	}

	private void userLog() {
		System.out.println("The six recommendations we provide to you dear user " + userID + ":");
		System.out.println("movieID" + "\t" + "mRating" + " " + "userID" + "\t" + "trustValue");
		for (int i = 0; i < 6; i++) {
			System.out.println(recommendations.get(i));
		}
		System.out.println();
	}

	public void updateUserTrust(Recommendation r, int userRating) {
		double trust = r.trustValue;
		int trusteeID = r.userID;
		double trusteeRating = r.movieRating;
		double averageRating = movieAverages.get(r.movieID);
		if (Math.abs(trusteeRating - userRating) < Math.abs(averageRating - userRating)) {
			trust = trust + trust * (1 - Math.abs(trusteeRating - userRating) / 5);
		} else {
			trust = trust - trust * (Math.abs(trusteeRating - userRating) / 5);
		}
		try {
			Statement stmt = dbConnection.createStatement();
			String trustCheck = "SELECT * FROM trust WHERE trustorID=" + userID + " AND trusteeID=" + trusteeID + ";";
			ResultSet rs = stmt.executeQuery(trustCheck);
			if (rs.next()) {// If there is a relation between this user and
							// recommended user
				String updateTrust = "UPDATE trust SET trustValue=" + trust + "WHERE trustorID=" + userID
						+ " AND trusteeID=" + trusteeID + ";";
				stmt.executeUpdate(updateTrust);
			} else {
				String insertTrust = "INSERT INTO trust (trustorID, trusteeID, trustValue) VALUES (" + userID + ", "
						+ trusteeID + ", " + trust + ");";
				stmt.executeUpdate(insertTrust);
			}
			String insertMovie = "INSERT INTO movie_ratings (userID, movieID, genreID, movieRating) VALUES (" + userID
					+ ", " + r.movieID + ", " + selectedGenre + ", " + userRating + ");";
			stmt.executeUpdate(insertMovie);
			System.out.println("Your updated friends list:");
			System.out.println("UserID\tTrustValue");
			String friends = "SELECT trusteeID, trustValue FROM trust WHERE trustorID = " + userID + ";";
			rs = stmt.executeQuery(friends);
			// while(rs.next())
			// System.out.println(rs.getInt("trusteeID")+"\t"+rs.getDouble("trustValue"));
			System.out.println();
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in db connection of updatingTrust");
			e.printStackTrace();
		}
	}
}
