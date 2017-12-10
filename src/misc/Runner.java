package misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import agents.GenreNegotiator;
import agents.MovieAdvisor;
import agents.MovieNegotiator;
import agents.Negotiator;
import agents.WeeklyPlanner;

public class Runner {

	public static final int PLANNED_DAY_COUNT = 6;
	public static int lastObligedDate = 0;
	public static int lastObligedEndTime = 0;

	public static void main(String[] args) throws Exception {
	
		
		MovieAdvisor advisor4218 = new MovieAdvisor(4218);
		MovieAdvisor advisor2116 = new MovieAdvisor(2116);
		MovieAdvisor advisor9866 = new MovieAdvisor(9866);
		MovieAdvisor advisor4434 = new MovieAdvisor(4434);
		MovieAdvisor advisor = advisor4218;
		
		
		// Recommendations for Scenario 1-2
		/*
		ArrayList<Recommendation> recommendations4218 = new ArrayList<Recommendation>();
		recommendations4218.add(new Recommendation(10239,433,4.8224,0.7,4.5));
		recommendations4218.add(new Recommendation(4215,44,4.124,0.7,4.0));
		recommendations4218.add(new Recommendation(2210,135,4.8854,0.7,4.54));
		recommendations4218.add(new Recommendation(14404,423,4.6824,0.7,4.25));
		recommendations4218.add(new Recommendation(1927,4563,4.8224,0.7,4.35));
		recommendations4218.add(new Recommendation(4292,413,4.9,0.7,4.55));
		advisor.setRecommendations(recommendations4218);
		
		ArrayList<Recommendation> recommendations2116 = new ArrayList<Recommendation>();
		recommendations2116.add(new Recommendation(7573,433,4.8824,0.7,4.66));
		recommendations2116.add(new Recommendation(8102,44,4.8824,0.7,4.62));
		recommendations2116.add(new Recommendation(6112,135,4.8824,0.7,4.4884));
		recommendations2116.add(new Recommendation(7630,423,4.8824,0.7,4.37));
		recommendations2116.add(new Recommendation(4653,4563,4.8824,0.7,4.7));
		recommendations2116.add(new Recommendation(3173,413,4.8824,0.7,4.8));
		
		ArrayList<Recommendation> recommendations9866 = new ArrayList<Recommendation>();
		recommendations9866.add(new Recommendation(14325,543,4.8824,0.7,4.9));
		recommendations9866.add(new Recommendation(8404,4138,4.8824,0.7,4.3));
		recommendations9866.add(new Recommendation(6363,343,4.8824,0.7,4.6));
		recommendations9866.add(new Recommendation(5340,473,4.8824,0.7,4.7));
		recommendations9866.add(new Recommendation(10126,113,4.8824,0.7,4.4));
		recommendations9866.add(new Recommendation(7815,433,4.8824,0.7,4.5));
		
		
		ArrayList<Recommendation> recommendations4434 = new ArrayList<Recommendation>();
		recommendations4434.add(new Recommendation(4244,41,4.4124,0.7,4.52));
		recommendations4434.add(new Recommendation(2218,443,4.8824,0.7,4.56));
		recommendations4434.add(new Recommendation(4102,453,4.8824,0.7,4.75));
		recommendations4434.add(new Recommendation(10167,423,4.8824,0.7,4.451));
		recommendations4434.add(new Recommendation(7715,463,4.8824,0.7,4.55));
		recommendations4434.add(new Recommendation(6210,2463,4.8824,0.7,4.35));
		
		// Negotiators
		
		//FOR SCENARIO 1-2
		
		Negotiator neg2116 = new MovieNegotiator(2116, 2, true, recommendations2116);
		Negotiator neg4218 = new MovieNegotiator(4218, 2, true, recommendations4218);
		Negotiator neg9866 = new MovieNegotiator(9866, 2, true, recommendations9866);
		Negotiator neg4434 = new MovieNegotiator(9866, 2, true, recommendations4434);
		*/
		
		//SCENARIO 3
		//Genres for Scenario 3
		ArrayList<Integer> genres4218 = advisor4218.getGenrePreferenceOrder();
		ArrayList<Integer> genres2116 =	advisor2116.getGenrePreferenceOrder();
		ArrayList<Integer> genres9866 = advisor9866.getGenrePreferenceOrder();

		
		Negotiator Gneg2116 = new GenreNegotiator(2116, 1, true, genres2116);
		Negotiator Gneg4218 = new GenreNegotiator(4218, 1, true, genres4218);
		Negotiator Gneg9866 = new GenreNegotiator(9866, 1, true, genres9866);

		ArrayList<Negotiator> genreNegotiators = new ArrayList<Negotiator>();
		genreNegotiators.add(Gneg4218);
		genreNegotiators.add(Gneg2116);
		genreNegotiators.add(Gneg9866);
		Bazaar genreBazaar = new Bazaar(true, genreNegotiators);
		Offer selectedGenre = genreBazaar.runNegotiation();

		//SET AGREED GENRE THEN WORK ON MOVIE
		advisor4218.setPreDeterminedGenre(selectedGenre.getTypeID());
		advisor2116.setPreDeterminedGenre(selectedGenre.getTypeID());
		advisor9866.setPreDeterminedGenre(selectedGenre.getTypeID());
		advisor4218.execute();
		advisor2116.execute();
		advisor9866.execute();
		
		Negotiator neg2116 = new MovieNegotiator(4218, 1, true, advisor4218.getRecommendations());
		Negotiator neg4218 = new MovieNegotiator(2116, 1, true, advisor2116.getRecommendations());
		Negotiator neg9866 = new MovieNegotiator(9866, 1, true, advisor9866.getRecommendations());
		
		ArrayList<Negotiator> negotiators = new ArrayList<Negotiator>();
		negotiators.add(neg4218);
		negotiators.add(neg2116);
		negotiators.add(neg9866);
		//negotiators.add(neg4434);
		Bazaar bazaar = new Bazaar(true, negotiators);
		Offer selectedMovie = bazaar.runNegotiation();
		System.out.println("bbb");
		if(selectedMovie == null) {
			return;
		}
		
		Scanner scan = new Scanner(new File("events.tsv"));
		Object[] readEvents = Utils.readEvents(scan, selectedMovie);
		lastObligedDate = (int) readEvents[0];
		lastObligedEndTime = (int) readEvents[1];
		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<Event>> events = (ArrayList<ArrayList<Event>>) readEvents[2];
		// Utils.printEvents(events, 0);
		scan = new Scanner(new File("./preferences.tsv"));
		HashMap<String, Integer> prefs = Utils.readPrefs(scan);
		// Utils.printPrefs(prefs);

		scan = new Scanner(System.in);
		int returnedDate = 99;
		int watchedMoviesNum = 0;
		// Day Iterator
		for (int i = 0; i < PLANNED_DAY_COUNT && (i < returnedDate + 1); i++) {
			System.out.println("Good Morning!");
			System.out.println("Today is July " + (i + 16) + "\nHere is your program for following days");
			Object[] temp = WeeklyPlanner.execute(i, lastObligedDate, lastObligedEndTime, events, prefs,
					watchedMoviesNum);
			returnedDate = (int) temp[0];
			boolean isMovieWatched = (boolean) temp[1];
			int recNum = (int) temp[2];
			watchedMoviesNum = (int) temp[3];
			System.out.println("Choose one to continue: \n" + "0: No action\n" + "1: Add Event\n" + "2: Update Event\n"
					+ "3: Update preference\n" + "4: Delete Event");
			int choice = scan.nextInt();
			switch (choice) {
			case 1:
				// Utils.printEvents(events);
				Utils.addEvent(scan, events);
				// Utils.printEvents(events);
				i--;
				break;
			case 2:
				Utils.printEvents(events, 0);
				Utils.updateEvent(scan, events);
				// Utils.printEvents(events);
				i--;
				break;
			case 3:
				Utils.updatePref(scan, prefs);
				i--;
				break;
			case 4:
				Utils.printEvents(events, 0);
				Utils.deleteEvent(scan, events);
				i--;
				break;
			default:
				if (isMovieWatched) {
					System.out.println("How was your movie? 0 - 5");
					int rating = scan.nextInt();
					ArrayList<Recommendation> recommends = advisor.getRecommendations();
					advisor.updateUserTrust(recommends.get(recNum), rating);
				}
				break;
			}

		}
		scan.close();

	}

}
