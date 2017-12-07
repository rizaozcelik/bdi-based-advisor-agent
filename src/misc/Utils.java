package misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import agents.MovieAdvisor;

public class Utils {
	public static ArrayList<Event> allObligedEvents = new ArrayList<Event>();
//	private static int lastDate;

	public static void printEvents(ArrayList<ArrayList<Event>> events) {
		// System.out.println(header);
		for (ArrayList<Event> daily : events) {
			Collections.sort(daily);
			System.out.println("Day " + (daily.get(0).date + 16) + ":");
			for (Event e : daily) {
				System.out.println(e);
			}
			System.out.println();
		}
	}

	public static void printEvents(ArrayList<ArrayList<Event>> events, int i) {
		// System.out.println(header);
		for (ArrayList<Event> daily : events) {
			Collections.sort(daily);
			System.out.println("Day " + (daily.get(0).date + 16) + ":");
			for (Event e : daily) {
				System.out.print(e.ID + "  ");
				System.out.println(e);
			}
			System.out.println();
		}
	}

	public static void printPrefs(HashMap<String, Integer> prefs) {
		for (Entry<String, Integer> entry : prefs.entrySet()) {
			System.out.println(entry.getKey() + '\t' + entry.getValue());
		}

	}

	public static Object[] readEvents(Scanner scan, MovieAdvisor advisor) {
		scan.nextLine();
		ArrayList<ArrayList<Event>> events = new ArrayList<ArrayList<Event>>();
		for (int i = 0; i < Runner.PLANNED_DAY_COUNT; i++) {
			events.add(new ArrayList<Event>());
		}
		int lastDate = 0;
		int lastObligedDate = 0;
		int lastObligedEndTime = 0;
		int greatestID = 0;
		while (scan.hasNextLine()) {
			String[] line = scan.nextLine().split("\t");
			// Store date as 0,1,2...
			int eventID = Integer.parseInt(line[0]);
			if (eventID > greatestID) {
				greatestID = eventID;
			}
			Event event = new Event(eventID, line[1].toLowerCase(), Integer.parseInt(line[2]) - 16,
					line[3].toLowerCase(), Integer.parseInt(line[4]), line[5].equals("1"));
			if (lastDate != event.date) {
				lastDate++;
			}
			if (event.isObliged) {
				allObligedEvents.add(event);
				if (lastObligedDate < event.date) {
					lastObligedDate = event.date;
					lastObligedEndTime = event.endTime;
				} else if (lastObligedDate == event.date) {
					if (lastObligedEndTime < event.endTime)
						lastObligedEndTime = event.endTime;
				}
			}
			events.get(lastDate).add(event);
		}
//		Utils.lastDate = lastDate;
		addMovies(lastDate, greatestID, events, advisor);

		scan.close();
		return new Object[] { lastObligedDate, lastObligedEndTime, events };
	}

	// Adds recommended movies to each days event list
	private static void addMovies(int lastDate, int greatestID, ArrayList<ArrayList<Event>> events, MovieAdvisor advisor) {
		ArrayList<Recommendation> recommendations = advisor.getRecommendations();
		HashMap<Integer, Double> movieAverages = advisor.getMovieAverages();
		for (int k = 0; k < lastDate + 1; k++) {
			for (int i = 0; i < lastDate + 1; i++) {
				Recommendation ithRec = recommendations.get(i);
				MovieEvent m = new MovieEvent(greatestID + 1 + i + k * (lastDate + 1), "movie", k, "19-21",
						(int) (movieAverages.get(ithRec.movieID) * 20), false, i, ithRec.movieID);
				events.get(k).add(m);
			}
		}
	}

	public static HashMap<String, Integer> readPrefs(Scanner scan) {
		HashMap<String, Integer> prefs = new HashMap<String, Integer>();
		scan.nextLine();
		while (scan.hasNextLine()) {
			String[] line = scan.nextLine().split("\t");
			prefs.put(line[0].toLowerCase(), Integer.parseInt(line[1]));
		}
		return prefs;
	}

	public static void updatePref(Scanner scan, HashMap<String, Integer> prefs) {
		String choice = "";
		while (!choice.equals("q")) {
			System.out.println("Enter name and updated value");
			String pref = scan.next().toLowerCase();
			int value = scan.nextInt();
			prefs.put(pref, value);
			System.out.println("Type q to quit, c to continue");
			choice = scan.next();
		}
	}

	public static void updateEvent(Scanner scan, ArrayList<ArrayList<Event>> events) {
		String choice = "";
		while (!choice.equals("q")) {
			System.out.println("Enter event id and new values");
			int ID = scan.nextInt();
			Event e = null;
			boolean found = false;
			for (int i = 0; i < events.size() && !found; i++) {
				ArrayList<Event> daily = events.get(i);
				for (int j = 0; j < daily.size() && !found; j++) {
					if (daily.get(j).ID == ID) {
						e = daily.get(j);
						found = true;
						daily.remove(e);
						if (e.isObliged) {
							allObligedEvents.remove(e);
							Runner.lastObligedDate = 0;
							Runner.lastObligedEndTime = 0;
							for (Event event : allObligedEvents) {
								if (Runner.lastObligedDate < event.date) {
									Runner.lastObligedDate = event.date;
									Runner.lastObligedEndTime = event.endTime;
								} else if (Runner.lastObligedDate == event.date) {
									if (Runner.lastObligedEndTime < event.endTime)
										Runner.lastObligedEndTime = event.endTime;
								}
							}
						}
						break;
					}
				}

			}
			System.out.println("Date?:");
			int date = scan.nextInt();
			System.out.println("Time?:");
			String time = scan.next();
			System.out.println("Quality?:");
			int quality = scan.nextInt();
			System.out.println("Obligation?:");
			int obligation = scan.nextInt();
			e.date = date - 16;
			e.endTime = Integer.parseInt(time.split("-")[1]);
			e.startTime = Integer.parseInt(time.split("-")[0]);
			e.quality = quality;
			e.isObliged = obligation == 1;
			if (e.isObliged) {
				allObligedEvents.add(e);
				if (Runner.lastObligedDate < e.date) {
					Runner.lastObligedDate = e.date;
					Runner.lastObligedEndTime = e.endTime;
				} else if (Runner.lastObligedDate == e.date) {
					if (Runner.lastObligedEndTime < e.endTime)
						Runner.lastObligedEndTime = e.endTime;
				}
			}
			events.get(date - 16).add(e);
			System.out.println("Type q to quit, c to continue");
			choice = scan.next();
		}
	}

	public static void deleteEvent(Scanner scan, ArrayList<ArrayList<Event>> events) {
		String choice = "";
		while (!choice.equals("q")) {
			System.out.println("Enter event id that you want to remove:");
			int ID = scan.nextInt();
			Event e = null;
			boolean found = false;
			for (int i = 0; i < events.size() && !found; i++) {
				ArrayList<Event> daily = events.get(i);
				for (int j = 0; j < daily.size() && !found; j++) {
					if (daily.get(j).ID == ID) {
						e = daily.get(j);
						found = true;
						daily.remove(e);
						if (e.isObliged) {
							allObligedEvents.remove(e);
							Runner.lastObligedDate = 0;
							Runner.lastObligedEndTime = 0;
							for (Event event : allObligedEvents) {
								if (Runner.lastObligedDate < event.date) {
									Runner.lastObligedDate = event.date;
									Runner.lastObligedEndTime = event.endTime;
								} else if (Runner.lastObligedDate == event.date) {
									if (Runner.lastObligedEndTime < event.endTime)
										Runner.lastObligedEndTime = event.endTime;
								}
							}
						}
						break;
					}
				}

			}
			System.out.println("Type q to quit, c to continue");
			choice = scan.next();
		}
	}

	public static void addEvent(Scanner scan, ArrayList<ArrayList<Event>> events) {
		String choice = "";
		while (!choice.equals("q")) {
			System.out.println("Event ID");
			int ID = scan.nextInt();
			System.out.println("Event type?:");
			String type = scan.next().toLowerCase();
			System.out.println("Date?:");
			int date = scan.nextInt();
			System.out.println("Time?:");
			String time = scan.next();
			System.out.println("Quality?:");
			int quality = scan.nextInt();
			System.out.println("Obligation?:");
			int obligation = scan.nextInt();
			boolean isObliged = obligation == 1;
			events.get(date - 16).add(new Event(ID, type, date, time, quality, isObliged));
			System.out.println("Type q to quit, c to continue");
			choice = scan.next();
		}

	}
}
