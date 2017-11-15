package agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

public class Advisor {

	private static final int USER_ID = 1;

	private static final String DB_URL = "jdbc:mysql://localhost/MOVIE";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "12345";
	private static Connection dbConnection;

	public static void execute() {
		try {
			if (initDB()) {
				int selectedGenre = adviseGenre();
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
				return i;
			}
			runningSum += weights[i];
		}
		rs.close();
		stmt.close();
		return -1;
	}
}
