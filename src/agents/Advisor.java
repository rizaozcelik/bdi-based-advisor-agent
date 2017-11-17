package agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;

public class Advisor {

	private static final int USER_ID = 1;

	private static final String DB_URL = "jdbc:mysql://localhost/movie";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "12345";
	private static Connection dbConnection;
	private static int genre = 0;
	private static Double averageRating; //Average rating of the recommended movie
	private static ArrayList<double[]> friendRecommendations; //Movies recommended by friend
	private static ArrayList<double[]> friendsFriendRecommendations;
	private static ArrayList<double[]> professionalRecommendations;
	private static Double[] recommendedMovie; // NOT IMPLEMENTED HOW TO ACQUIRE
	private static int genreProfessional;
	
	public static void execute() {
		try {
			if (initDB()) {
				int selectedGenre = adviseGenre();
				friendsRecommendation();
			} else {
				System.out.println("Error in DB connection");
			}
			dbConnection.close();
		} catch (SQLException sql) {
			System.out.println("Error in DB connection in advise genre");
		}
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
	private static int adviseGenre() throws SQLException {
		Statement stmt = dbConnection.createStatement();
		/*
		 * String mostPopularGenreQuery= "SELECT GENREID FROM " +
		 * "(SELECT GENREID, AVG(MOVIERATING) AS AVG_RATING FROM " +
		 * "MOVIE_RATINGS WHERE MOVIERATING > 3.5 AND USERID = " + USER_ID +
		 * " GROUP BY GENREID ORDER BY AVG_RATING DESC LIMIT 1) tmp";
		 */
		// System.out.println(statement);
		String averagesQuery = "SELECT GENREID,AVG(MOVIERATING) FROM "
				+ "MOVIE_RATINGS WHERE MOVIERATING > 3.5  AND USERID= " + USER_ID + " GROUP BY GENREID;";

		double[] weights = new double[17];
		Arrays.fill(weights, 1);
		double weightSum = 17;
		ResultSet rs = stmt.executeQuery(averagesQuery);

		while (rs.next()) {
			int genreID = (int) rs.getDouble("GENREID");
			double avgRating = rs.getDouble("AVG(MOVIERATING)");
			weights[genreID - 1] = avgRating;
			weightSum = weightSum + avgRating - 1;
			System.out.println("Genre: " + genreID + " Rating: " + avgRating);
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
				genre = i;
				return i;
			}
			runningSum += weights[i];
		}
		rs.close();
		stmt.close();
		return -1;
	}
	
	//Returns movie ID and movie rate for
	private static int friendsRecommendation() throws SQLException{
		Statement stmt = dbConnection.createStatement();
		String friendRecommendationQuery = "SELECT movie_ratings.movieID, movie_ratings.userID, movie_ratings.movieRating, trust.trustValue, movie_ratings.movieRating*trust.trustValue as Watchability " + 
										   "FROM trust INNER JOIN movie_ratings ON movie_ratings.userID = trust.trusteeID " + 
										   "WHERE trust.trustorID = "+ USER_ID +" AND movie_ratings.genreID = "+genre+" ORDER BY Watchability DESC;";
		
		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		friendRecommendations.clear();
		while (rs.next()) {
			double[] friendRecommendation = new double[4];
			friendRecommendation[0] = rs.getDouble("movie_ratings.movieID");
			friendRecommendation[1] = rs.getDouble("movie_ratings.userID");
			friendRecommendation[2] = rs.getDouble("movie_ratings.movieRating");
			friendRecommendation[3] = rs.getDouble("trust.trustValue");
			friendRecommendations.add(friendRecommendation);
		}
		return 1;
	}
	
	private static int friendsfriendRecommendation() throws SQLException{
		Statement stmt = dbConnection.createStatement();
		String friendRecommendationQuery =	"SELECT moviesbygenre.movieID, moviesbygenre.userID, moviesbygenre.movieRating, friendsfriend.trustValue, moviesbygenre.movieRating*friendsfriend.trustValue as Watchability " + 
											"FROM (SELECT friends.trusteeID as trusteeID, me.trustValue*friends.trustValue as trustValue " + 
											"	   FROM (SELECT * FROM trust WHERE trust.trustorID = "+USER_ID+") as me " + 
											"		     INNER JOIN " + 
											"            trust as friends " + 
											"	   WHERE me.trusteeID = friends.trustorID) " + 
											"	   AS friendsfriend " + 
											"	   INNER JOIN " + 
											"	  (SELECT * " + 
											"      FROM movie_ratings " + 
											"      WHERE movie_ratings.genreID = "+genre+") " + 
											"      AS moviesbygenre " + 
											"ON moviesbygenre.userID = friendsfriend.trusteeID "+
											"ORDER BY Watchability DESC;";
		ResultSet rs = stmt.executeQuery(friendRecommendationQuery);
		friendsFriendRecommendations.clear();
		while (rs.next()) {
			double[] friendsFriendRecommendation = new double[4];
			friendsFriendRecommendation[0] = rs.getDouble("movie_ratings.movieID");
			friendsFriendRecommendation[1] = rs.getDouble("movie_ratings.userID");
			friendsFriendRecommendation[2] = rs.getDouble("movie_ratings.movieRating");
			friendsFriendRecommendation[3] = rs.getDouble("trust.trustValue");
			friendsFriendRecommendations.add(friendsFriendRecommendation);
		}
		return 1;
	}
	
	
	private static int genreProfessionalSearch() throws SQLException{
		Statement stmt = dbConnection.createStatement();
		String genreProfessionalSearchQuery = "SELECT userID, COUNT(*) AS MovieRateAmount " + 
											  "FROM movie_ratings " + 
											  "WHERE genreID = "+genre+" AND NOT userID IN (SELECT trusteeID FROM trust WHERE trustorID = "+USER_ID+") " + 
											  "GROUP BY userID " + 
											  "ORDER BY MovieRateAmount DESC " + 
											  "LIMIT 1;";
		ResultSet rs = stmt.executeQuery(genreProfessionalSearchQuery);
		rs.next();
		genreProfessional = (int)rs.getDouble("userID");
		return 1;
	}
	
	private static int professionalRecommendation() throws SQLException{
		Statement stmt = dbConnection.createStatement();
		String professionalRecommendationQuery = "SELECT movie_ratings.movieID, movie_ratings.movieRating " + 
											  	 "FROM movie_ratings " + 
											  	 "WHERE userID ="+genreProfessional+" AND genreID ="+genre +" "+ 
											  	 "ORDER BY movieRating DESC;";
		
		ResultSet rs = stmt.executeQuery(professionalRecommendationQuery);
		while(rs.next()) {
			double[] friendsFriendRecommendation = new double[4];
			friendsFriendRecommendation[0] = rs.getDouble("movie_ratings.movieID");
			friendsFriendRecommendation[1] = genreProfessional;
			friendsFriendRecommendation[2] = rs.getDouble("movie_ratings.movieRating");
			friendsFriendRecommendation[3] = 0.5;
			friendsFriendRecommendations.add(friendsFriendRecommendation);
		}
		return 1;
	}
	
	private static int recommendedMovieAverage() throws SQLException{
		Statement stmt = dbConnection.createStatement();
		String movieAverageQuery = "SELECT movieID, AVG(movieRating) as averageRating, COUNT(movieRating) as numberOfRatings " + 
									"FROM movie_ratings " + 
									"WHERE movieID = "+recommendedMovie+";";
		ResultSet rs = stmt.executeQuery(movieAverageQuery);
		rs.next();
		averageRating = rs.getDouble("averageRating");
		return 1;
	}
	
	
}
