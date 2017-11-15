package misc;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import agents.Advisor;
import agents.WeeklyPlanner;

public class Runner {

	public static final int PLANNED_DAY_COUNT = 6;
	public static int lastObligedDate = 0;
	public static int lastObligedEndTime = 0;
	
	public static void main(String[] args) throws Exception {
		Advisor.execute();
		int x = 1;
		if(x == 1){
			System.exit(1);
		}
		Scanner scan = new Scanner(new File("./preferences.tsv"));
		HashMap<String, Integer> prefs = Utils.readPrefs(scan);
//		Utils.printPrefs(prefs);

		scan = new Scanner(new File("events.tsv"));
		Object[] readEvents = Utils.readEvents(scan);
		lastObligedDate = (int) readEvents[0];
		lastObligedEndTime = (int) readEvents[1];
		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<Event>> events = (ArrayList<ArrayList<Event>>) readEvents[2];

		scan = new Scanner(System.in);
		int returnedDate = 99;
		// Day Iterator
		for (int i = 0; i < PLANNED_DAY_COUNT && (i < returnedDate+1); i++) {
			System.out.println("Good Morning!");
			System.out.println("Today is July " + (i + 16) + "\nHere is your program for following days");
			returnedDate = WeeklyPlanner.execute(i, lastObligedDate, lastObligedEndTime, events, prefs);
			System.out.println("Choose one to continue: \n"
					+ "0: No action\n"
					+ "1: Add Event\n"
					+ "2: Update Event\n"
					+ "3: Update preference\n"
					+ "4: Delete Event");
			int choice = scan.nextInt();
			switch (choice) {
			case 1:
				//Utils.printEvents(events);
				Utils.addEvent(scan,events);
				//Utils.printEvents(events);
				i--;
				break;
			case 2:
				Utils.printEvents(events,0);
				Utils.updateEvent(scan,events);
				//Utils.printEvents(events);
				i--;
				break;
			case 3:
				Utils.updatePref(scan,prefs);
				i--;
				break;
			case 4:
				Utils.printEvents(events,0);
				Utils.deleteEvent(scan, events);
				i--;
				break;
			default:
				break;
			}
			
		}

		scan.close();

	}

}
