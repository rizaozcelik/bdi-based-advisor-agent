package agents;
import java.util.ArrayList;
import java.util.HashMap;

import misc.Event;
import misc.MovieEvent;
import misc.Runner;
import misc.Utils;

public class WeeklyPlanner {
	public static ArrayList<Event> erasedReturns = new ArrayList<Event>();
	public static int execute(int currDate, int lastObligedDate, int lastObligedEndTime,
							  ArrayList<ArrayList<Event>> events, HashMap<String, Integer> prefs) {
		int lastDate = 0;
		int plannedMoviesNum =0;
		if(currDate > lastObligedDate)
			lastDate = currDate + 1;
		else
			lastDate = lastObligedDate + 1;
		
		ArrayList<ArrayList<Event>> plannedEvents = new ArrayList<ArrayList<Event>>();
		
		//handle erased returns needed for the cases where last obliged date has changed.
		for(Event e : erasedReturns) {
			if(e.date>Runner.lastObligedDate)
				events.get(e.date).add(e);
			else if(e.date==Runner.lastObligedDate && e.endTime > lastObligedEndTime)
				events.get(e.date).add(e);
		}
		
		for (int l = currDate; l < lastDate; l++) {
			boolean[] resetHours = { false, false, false, false, false, false, false, false, false, false, false,
					false }; // used to reset hours to default. Default =
								// empty+obliged Events
			ArrayList<Event> daysEvents = events.get(l);
			ArrayList<Event> acceptedEvents = new ArrayList<Event>();
			ArrayList<Event> obligedEvents = new ArrayList<Event>();

			// Handle obliged Events first
			for (int z = 0; z < daysEvents.size(); z++) {
				Event e = daysEvents.get(z);
				if (e.isObliged && !obligedEvents.contains(e)) {
					obligedEvents.add(e);
					for (int k = e.startTime - 12; k < e.endTime - 12; k++) {
						resetHours[k] = true;
					}
				}
				if (e.type.equals("return")) {
					if (e.date < lastObligedDate) {
						daysEvents.remove(e);
						erasedReturns.add(e);
						z--;
					} else if (e.date == lastObligedDate) {
						if (e.startTime < lastObligedEndTime) {
							daysEvents.remove(e);
							erasedReturns.add(e);
							z--;
						}
					}
				}
			}
			
			
			

			int numOfEvents = daysEvents.size(); // Number of events in the
													// selected day
			int caseNum = (int) Math.pow(2, numOfEvents); // Number of subsets
															// that the event
															// set of the day
															// have
			int pleasure = 0;
			System.out.println(numOfEvents);
			for (int k = 1; k < caseNum; k++) {
				ArrayList<Event> tempAccepted = new ArrayList<Event>();
				int t = k;
				int tempPleasure = 0;
				boolean[] tempHours = resetHours.clone();

				for (int j = 0; j < numOfEvents; j++) { // binary representation
														// of the kth subset
					if (t / 2 != 0) {
						if (t % 2 == 1) {
							Event e = daysEvents.get(j);
							int startTime = e.startTime - 12;
							int endTime = e.endTime - 12;
							boolean brk = false;
							for (int time = startTime; time < endTime; time++) {
								if (tempHours[time]) {
									brk = true;
									break;
								}
							} 
							if(e.getClass().getSimpleName().equals("MovieEvent")) {
								//If movie events have been included in previous days' plans n times, the movie recommendations until the nth recommendation are skipped.
								if(((MovieEvent)e).getRecommendationNumber() < plannedMoviesNum) {
									brk = true;
								}
							}
							if (!brk) {
								for (int time = startTime; time < endTime; time++) {
									tempHours[time] = true;
								}
								tempPleasure = tempPleasure + e.quality * prefs.get(e.type);
								tempAccepted.add(e);
							}
						}
					} else if (t % 2 == 1) {
						Event e = daysEvents.get(j);
						int startTime = daysEvents.get(j).startTime - 12;
						int endTime = daysEvents.get(j).endTime - 12;
						boolean brk = false;
						for (int time = startTime; time < endTime; time++) {
							if (tempHours[time]) {
								brk = true;
								break;
							}
						}
						if(e.getClass().getSimpleName().equals("MovieEvent")) {
							//If movie events have been included in previous days' plans n times, the movie recommendations until the nth recommendation are skipped.
							if(((MovieEvent)e).getRecommendationNumber() < plannedMoviesNum) {
								brk = true;
							}
						}
						if (!brk) {
							for (int time = startTime; time < endTime; time++) {
								tempHours[time] = true;
							}
							System.out.println(e.ID);
							System.out.println(e.type);
							System.out.println(e.quality);
							Utils.printPrefs(prefs);
							tempPleasure = tempPleasure + e.quality * prefs.get(e.type);
							tempAccepted.add(e);
						}
						break;
					}
					t = t / 2;
				}
				if (pleasure < tempPleasure) {
					pleasure = tempPleasure;
					acceptedEvents.clear();
					acceptedEvents.addAll(tempAccepted);
				}
				tempHours = resetHours.clone();
			}
			acceptedEvents.addAll(obligedEvents);
			plannedEvents.add(acceptedEvents);
			for (Event acceptedEvent : acceptedEvents){
				if(acceptedEvent.getClass().getSimpleName().equals("MovieEvent")) {
					plannedMoviesNum++;
				}
			}
			for (Event e : acceptedEvents) {
				if (e.type.equals("return")) {
					System.out.println();
					Utils.printEvents(plannedEvents);
					return e.date;
				}
			}
			if(l == lastDate-1) {
				lastDate++;
			}
		}
		// At this point times for events are taken should print the plan
		System.out.println();
		Utils.printEvents(plannedEvents);
		return 21;
	}
}
