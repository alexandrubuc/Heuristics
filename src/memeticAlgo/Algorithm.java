package memeticAlgo;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import definitions.Course;
import definitions.Curriculum;
import definitions.KeyDayTime;
import definitions.KeyDayTimeRoom;
import definitions.Room;
import xmlParser.Parser;
import initialPopulation.populationGeneration;

public class Algorithm {
	
	 public static Map<KeyDayTimeRoom,Course> parent_a = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> parent_b = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> child_a = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> child_b = new HashMap<>(1000);
	 public static List<Map<KeyDayTimeRoom,Course>> population = new ArrayList<>(50);
	 double crossoverRate = 0.8;
	 public static Integer numDays;
	 public static Integer timeslotsPerDay;
	 public static Integer popSize;
	public static void main(String[] args) {

		//Debug
		System.out.println("You just entered the world of memeticAlgo!!");
		/**
		 * Call the main method of the populationGeneration class so as so generate the starting population == the parents
		 * have the main method copy the population into local population here, numDays and timeslotsPerDay
		 */
		String[] useless_args = new String[1];
		useless_args = null;
		initialPopulation.populationGeneration.main_(useless_args);
		
		/**
		 *  
		 *  -------------- CROSSOVER --------------
		 *  
		 */
		
		// with crossoverRate probability there will be a crossover
		SecureRandom rangen = new SecureRandom();
		
		if (rangen.nextInt(100) < 80) {
			long crossover_start = System.nanoTime();
		// select two random parents from population 
		int whichParent_A = rangen.nextInt(popSize);
		parent_a = population.get(whichParent_A);
		//make sure we don't get the same parent twice
		int whichParent_B = rangen.nextInt(popSize);
		while (whichParent_A == whichParent_B) {
			whichParent_B = rangen.nextInt(popSize);
		}
		parent_b = population.get(whichParent_B);
		
		//crossover is performed by inserting all events from a random timeslot in one parent to another random timeslot in the other parent to produce one child; viceversa for the other child
		Integer crossDay_A = rangen.nextInt(numDays);
		Integer crossTimeslot_A = rangen.nextInt(timeslotsPerDay);
		Integer crossDay_B = rangen.nextInt(numDays);
		Integer crossTimeslot_B = rangen.nextInt(timeslotsPerDay);
		
		/**
		 *  try to insert all events from crossDay_A and crossTimeslot_A from parent A in parent B to produce child B
		 */
		child_b  = parent_b;
		// check first if/how many and which places are even available (course still null) in parent_b in the corresponding day and timeslots
		final List<Map.Entry<KeyDayTimeRoom,Course>> correspondingDay_entries = child_b.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_B && entry.getKey().getTimeslot() == crossTimeslot_B).collect(Collectors.toList());
		List<Map.Entry<KeyDayTimeRoom,Course>> available_entries = correspondingDay_entries.stream().filter(entry -> entry.getValue() == null).collect(Collectors.toList());
		int numAvailEntries = available_entries.size();
		// select the entries which may be inserted
		final List<Map.Entry<KeyDayTimeRoom,Course>> insert_entries = parent_a.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_A && entry.getKey().getTimeslot() == crossTimeslot_A).collect(Collectors.toList());
	
		// insert as many as possible, taking care not to violate any constraints related to the course
		// consider also the other courses which are already present in the timeslot!
		available_entries.forEach( avail -> {
			// get one course which is suitable for the KDTR entry
			List<Curriculum> bannedCurr =  new ArrayList<>(100);
			List<Integer> bannedTeacher = new ArrayList<>(100);
			
				   correspondingDay_entries.stream().filter(entry -> entry.getValue() != null ).forEach(kdtr -> {
				   bannedCurr.addAll( kdtr.getValue().belongsToCurricula);
				   bannedTeacher.add(kdtr.getValue().teacherID);
			});
			
			Course[] CoursetoBeInserted = new Course[1];
			CoursetoBeInserted[0] = null;
			try {
				CoursetoBeInserted[0] = insert_entries.stream().filter(cand -> cand.getValue().constraintsTimeslot.contains(new KeyDayTime(crossDay_B,crossTimeslot_B)) == false && cand.getValue().constraintsRoom.contains(avail.getKey().getRoom()) == false ).filter( those -> Collections.disjoint(those.getValue().belongsToCurricula,bannedCurr) && bannedTeacher.contains(those.getValue().teacherID) == false).findAny().get().getValue();
			}
			catch (Exception ex) { // catch NullPointerExceptions
				CoursetoBeInserted[0] = null;
			} 
			
			if (CoursetoBeInserted[0] != null) {
				child_b.put(avail.getKey(), CoursetoBeInserted[0]);
				//removing the duplicate : due to the insertion above, one lecture must be taken away -> done randomly
				child_b.entrySet().stream().filter(aquellos -> aquellos.getValue().courseID == CoursetoBeInserted[0].courseID).findAny().get().setValue(null);
			}
		});
		
		//clear all used shit before!!!!!!!!
		
		/**
		 *  try to insert all events from crossDay_B and crossTimeslot_B from parent B in parent A to produce child A
		 */
		child_a  = parent_a;
		available_entries.clear();
		// check first if/how many and which places are even available (course still null) in parent_b in the corresponding day and timeslots
		 final List<Map.Entry<KeyDayTimeRoom,Course>> correspondingDay_entries__ = child_a.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_A && entry.getKey().getTimeslot() == crossTimeslot_A).collect(Collectors.toList());
		 available_entries = correspondingDay_entries__.stream().filter(entry -> entry.getValue() == null).collect(Collectors.toList());
		 numAvailEntries = available_entries.size();
		// select the entries which may be inserted
		 final List<Map.Entry<KeyDayTimeRoom,Course>> insert_entries__ = parent_b.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_B && entry.getKey().getTimeslot() == crossTimeslot_B).collect(Collectors.toList());
		
		// insert as many as possible, taking care not to violate any constraints related to the course
		// consider also the other courses which are already present in the timeslot!
		available_entries.forEach( avail -> {
			// get one course which is suitable for the KDTR entry
		   
			List<Curriculum> bannedCurr =  new ArrayList<>(100);
			List<Integer> bannedTeacher = new ArrayList<>(100);
			
			correspondingDay_entries__.stream().filter(entry -> entry.getValue() != null ).forEach(kdtr -> {
				   bannedCurr.addAll( kdtr.getValue().belongsToCurricula);
				   bannedTeacher.add(kdtr.getValue().teacherID);
			});
		
				Course[] CoursetoBeInserted = new Course[1];
				try {
					CoursetoBeInserted[0] = insert_entries__.stream().filter(cand -> cand.getValue().constraintsTimeslot.contains(new KeyDayTime(crossDay_A,crossTimeslot_A)) == false && cand.getValue().constraintsRoom.contains(avail.getKey().getRoom()) == false ).filter( those -> Collections.disjoint(those.getValue().belongsToCurricula,bannedCurr) && bannedTeacher.contains(those.getValue().teacherID) == false).findAny().get().getValue();
				}
				catch (Exception ex) { // catch NullPointerExceptions
					CoursetoBeInserted[0] = null;
				}
			if (CoursetoBeInserted[0] != null) {
				child_a.put(avail.getKey(), CoursetoBeInserted[0]);
				//removing the duplicate : due to the insertion above, one lecture must be taken away -> done randomly
				child_a.entrySet().stream().filter(aquellos -> aquellos.getValue().courseID == CoursetoBeInserted[0].courseID).findAny().get().setValue(null);
			}
		});
		
		long crossover_done = System.nanoTime() - crossover_start;
		System.out.println((double) crossover_done / 1000000000.0 + " seconds for the crossover algorithm");
		} // end crossover rate
		
		
		
		
		
		
		
	}

}
