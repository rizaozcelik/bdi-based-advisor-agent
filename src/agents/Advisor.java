package agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import misc.Recommendation;

public class Advisor {

	private static final int USER_ID = 1;

	private static final String DB_URL = "jdbc:mysql://localhost/movie";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "12345";
	private static Connection dbConnection;
	// private static int genre = 0;
	private static Double averageRating; // Average rating of the recommended
											// movie
	// private static ArrayList<double[]> friendRecommendations = new
	// ArrayList<double[]>(); // Movies
	// private static ArrayList<double[]> friendsFriendRecommendations;
	// private static ArrayList<double[]> professionalRecommendations;
	private static ArrayList<Recommendation> recommendations;
	private static HashSet<Integer> watchedMovies;
	private static Double[] recommendedMovie; // NOT IMPLEMENTED HOW TO ACQUIRE
	// private static int genreProfessional;

	public static double[] execute() {
		recommendations = new ArrayList<Recommendation>();
		watchedMovies = new HashSet<Integer>();
		double selectedMovieQuality = -1, selectedMoviePrefernce = -1;
		try {
			if (initDB()) {
				int selectedGenre = getGenreToWatch();
				getRecommendationFromFriend(selectedGenre);
				if (recommendations.isEmpty() || allMoviesWatched()) {
					// If no non-watched movie is advised by direct friends..
					getRecommendationFromFriendsOfFriends(selectedGenre);
					getProfessionalRecommendation(selectedGenre);
				} else {
					// Line to set recommended movie fields
					// TODO: update quality and prefs in planner(preferably) or runner, not here
					// update trust value of the random guy if his movie is watched.
				}
			} else {
				System.out.println("Error in DB connection");
			}
			dbConnection.close();
		} catch (SQLException sql) {
			System.out.println("Error in DB connection in methods");
		}
		return new double[] { selectedMovieQuality, selectedMoviePrefernce };
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
		/*
		 * String mostPopularGenreQuery= "SELECT GENREID FROM " +
		 * "(SELECT GENREID, AVG(MOVIERATING) AS AVG_RATING FROM " +
		 * "MOVIE_RATINGS WHERE MOVIERATING > 3.5 AND USERID = " + USER_ID +
		 * " GROUP BY GENREID ORDER BY AVG_RATING DESC LIMIT 1) tmp";
		 */
		// System.out.println(statement);
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
				// System.out.println("genre " + i + " result" + result + "
				// running" + runningSum);
				// genre = i;
				return i;
			}
			runningSum += weights[i];
		}
		rs.close();
		stmt.close();
		return -1;
	}

	// Returns movie ID and movie rate for
	private static void getRecommendationFromFriend(int selectedGenre) throws SQLException {
		Statement stmt = dbConnection.createStatement();
		String friendRecommendationQuery = "SELECT movie_ratings.movieID, movie_ratings.userID, movie_ratings.movieRating, trust.trustValue, movie_ratings.movieRating*trust.trustValue as Watchability "
				+ "FROM trust INNER JOIN movie_ratings ON movie_ratings.userID = trust.trusteeID "
				+ "WHERE trust.trustorID = " + USER_ID + " AND movie_ratings.genreID = " + selectedGenre
				+ " ORDER BY Watchability DESC;";

		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		while (rs.next()) {
			Recommendation recommendation = new Recommendation(rs.getInt("movie_ratings.movieID"),
					rs.getInt("movie_ratings.userID"), rs.getDouble("movie_ratings.movieRating"),
					rs.getInt("trust.trustValue"));
			// friendRecommendation[0] = rs.getDouble("movie_ratings.movieID");
			// friendRecommendation[1] = rs.getDouble("movie_ratings.userID");
			// friendRecommendation[2] =
			// rs.getDouble("movie_ratings.movieRating");
			// friendRecommendation[3] = rs.getDouble("trust.trustValue");
			recommendations.add(recommendation);
		}
		rs.close();
		stmt.close();
	}

	private static void getRecommendationFromFriendsOfFriends(int genre) throws SQLException {
		Statement stmt = dbConnection.createStatement();
		String friendRecommendationQuery = "SELECT moviesbygenre.movieID, moviesbygenre.userID, moviesbygenre.movieRating, friendsfriend.trustValue, moviesbygenre.movieRating*friendsfriend.trustValue as Watchability "
				+ "FROM (SELECT friends.trusteeID as trusteeID, me.trustValue*friends.trustValue as trustValue "
				+ "	   FROM (SELECT * FROM trust WHERE trust.trustorID = " + USER_ID + ") as me "
				+ "		     INNER JOIN " + "            trust as friends "
				+ "	   WHERE me.trusteeID = friends.trustorID) " + "	   AS friendsfriend " + "	   INNER JOIN "
				+ "	  (SELECT * " + "      FROM movie_ratings " + "      WHERE movie_ratings.genreID = " + genre + ") "
				+ "      AS moviesbygenre " + "ON moviesbygenre.userID = friendsfriend.trusteeID "
				+ "ORDER BY Watchability DESC;";
		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		// friendsFriendRecommendations.clear();
		while (rs.next()) {
			// double[] friendsFriendRecommendation = new double[4];
			// friendsFriendRecommendation[0] =
			// rs.getDouble("movie_ratings.movieID");
			// friendsFriendRecommendation[1] =
			// rs.getDouble("movie_ratings.userID");
			// friendsFriendRecommendation[2] =
			// rs.getDouble("movie_ratings.movieRating");
			// friendsFriendRecommendation[3] =
			// rs.getDouble("trust.trustValue");
			// friendsFriendRecommendations.add(friendsFriendRecommendation);
			Recommendation recommendation = new Recommendation(rs.getInt("movie_ratings.movieID"),
					rs.getInt("movie_ratings.userID"), rs.getDouble("movie_ratings.movieRating"),
					rs.getInt("trust.trustValue"));
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
				// double[] friendsFriendRecommendation = new double[4];
				// friendsFriendRecommendation[0] =
				// rs.getDouble("movie_ratings.movieID");
				// friendsFriendRecommendation[1] = genreProfessional;
				// friendsFriendRecommendation[2] =
				// rs.getDouble("movie_ratings.movieRating");
				// friendsFriendRecommendation[3] = 0.5;
				// friendsFriendRecommendations.add(friendsFriendRecommendation);
				Recommendation recommendation = new Recommendation(rs.getInt("movie_ratings.movieID"),
						genreProfessional, rs.getDouble("movie_ratings.movieRating"), 0.5);
				recommendations.add(recommendation);
			}
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			System.out.println("Error in db connection of professional");
			e.printStackTrace();
		}

	}

	private static int recommendedMovieAverage() throws SQLException {
		Statement stmt = dbConnection.createStatement();
		String movieAverageQuery = "SELECT movieID, AVG(movieRating) as averageRating, COUNT(movieRating) as numberOfRatings "
				+ "FROM movie_ratings " + "WHERE movieID = " + recommendedMovie + ";";
		ResultSet rs = stmt.executeQuery(movieAverageQuery);
		rs.next();
		averageRating = rs.getDouble("averageRating");
		return 1;
	}

	private static boolean allMoviesWatched() {
		for (Recommendation recommendation : recommendations) {
			if (!watchedMovies.contains(recommendation.movieID)) {
				return false;
			}
		}
		return true;
	}
}
