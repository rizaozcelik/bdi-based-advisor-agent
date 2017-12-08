package misc;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import agents.MovieAdvisor;
import agents.Negotiator;
import agents.WeeklyPlanner;

public class Runner {

	public static final int PLANNED_DAY_COUNT = 6;
	public static int lastObligedDate = 0;
	public static int lastObligedEndTime = 0;

	public static void main(String[] args) throws Exception {
		// Advisor.execute();
		MovieAdvisor advisor4218 = new MovieAdvisor(4218);
		// This the agent of 4218
		MovieAdvisor advisor = advisor4218;

		ArrayList<Recommendation> recommendations4218 = advisor4218.execute();
		MovieAdvisor advisor1 = new MovieAdvisor(1);		
		ArrayList<Recommendation> recommendations1 = advisor1.execute();
//		for(Recommendation r : recommendations1){
//			System.out.println(r);
//		}
//		for(Recommendation r : recommendations4218){
//			System.out.println(r);
//		}
		Negotiator neg4218 = new Negotiator(recommendations4218, 4218);
		neg4218.evaluate(new MovieOffer(1, 1, 79, 0, OfferResponse.Reject));
		Scanner scan = new Scanner(new File("events.tsv"));
		Object[] readEvents = Utils.readEvents(scan, advisor);
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
