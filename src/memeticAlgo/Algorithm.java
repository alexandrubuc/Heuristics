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
import hilfsFunktionen.Funktionen;
import xmlParser.Parser;
import initialPopulation.populationGeneration;

public class Algorithm {
	
	 public static Map<KeyDayTimeRoom,Course> parent_a = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> parent_b = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> child_a = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> child_b = new HashMap<>(1000);
	 public static List<Map<KeyDayTimeRoom,Course>> population = new ArrayList<>(50);
	 double crossoverRate = 0.8;
	 public static int numDays;
	 public static int timeslotsPerDay;
	 public static int popSize;
	 public static int numCourses;
	 public static int numRooms;
	 public static int numCurricula;
     public static List<Room> roomList = new ArrayList<>(100);
	 public static List<Curriculum> CurriculumList = new ArrayList<>(200);


	private int calcPenaltyScore(Map<KeyDayTimeRoom,Course> tempSol) {
		int hardScore = calcPenaltyScore_HARD(tempSol);
		int softScore = calcPenaltyScore_SOFT(tempSol);
		return hardScore + softScore;
	}

	private int calcPenaltyScore_HARD(Map<KeyDayTimeRoom,Course> tempSol) {
	// H1 : all lectures must be scheduled, and assigned to different periods
		// -> ensured by the constraints posed in the generation of the parents
	// H2: lectures of courses in same curriculum or taught by the same teacher must be assigned to different periods
		// -> ensured by the constraints posed in the generation of the parents
	// H3: -> No two lectures in the same room at the same time
		// -> ensured by the constraints posed in the generation of the parents
	// H4: -> if teacher is not available, no course can be scheduled
		// -> ensured by the constraints posed in the generation of the parents

	return 0;
	}

	private int calcPenaltyScore_SOFT(Map<KeyDayTimeRoom,Course> tempSol) {

		Map<Course,List<Integer>> course_days_Map = new HashMap<>(numCourses);
		Map<Course,List<Room>> course_room_Map = new HashMap<>(numCourses);
		Map<KeyDayTime,List<Curriculum>> kdt_curr_map = new HashMap<>(numDays*timeslotsPerDay);
		int[] score_soft = new int[1];
		score_soft[0] = 0;
		// try to only loop once through the Map
		for(int d=0; d<numDays; d++) {

			for(int t=0; t<timeslotsPerDay; t++) {
				KeyDayTime kdt = new KeyDayTime(d,t);

				for(int r=0; r<numRooms; r++ ) {
					Room room = roomList.get(r);
					KeyDayTimeRoom kdtr = new KeyDayTimeRoom(d,t,room);
					Course course = tempSol.get(kdtr);

					if (course != null) {
						//S1: Room capacity : number of students may not exceed the room capacity
						// constant is 1
						int st_overflow = course.students - room.getRoomCapacity();
						score_soft[0] = (st_overflow >0)? score_soft[0] + st_overflow : 0;

						//S2: Room stability : for each extra room in which the course has to be scheduled, count one
						// constant is 1
						List<Room> rooms = null;
						List<Room> already_there = course_room_Map.get(course);
						if (already_there != null) {
							//add to list
							rooms.addAll(already_there);
						}
						else {
							//first timer, no rooms yet added
							 rooms = new ArrayList<>(numRooms);
						}
						rooms.add(room);
						rooms = Funktionen.removeDuplicatesRoom(rooms);
						course_room_Map.put(course,rooms);
						/*int howManyRooms = rooms.size();
						score_soft = (howManyRooms > 1)? score_soft + howManyRooms -1 : score_soft;*/


						//S3: Minimum working days: lectures must be spread over a min of working days
						// constant is 5

						List<Integer> days = null;
						List<Integer> days_there = course_days_Map.get(course);
						if (days_there != null) {
							//add to list
							days.addAll(days_there);
						}
						else {
							//first timer, no days yet there
							days = new ArrayList<>(numDays);
						}
						days.add(d);
						days = Funktionen.removeDuplicatesInteger(days);
						course_days_Map.put(course,days);
						int howManyDays = days.size();
						/*int diff = howManyDays -  course.minWorkDays;
						score_soft = (diff < 0 )?  (score_soft - diff)*3 : score_soft ;*/

						//S4: Curriculum compactness: violation counted if there is one lecture not adjacent to any other lecture belonging to the same curriculum within the same day
						//constant is 2

						List<Curriculum> old = kdt_curr_map.get(kdt);
						List<Curriculum> newCurr = null;
						if (old != null) {
							//add to list
							newCurr.addAll(old);
						}
						else {
							//first time, no curricula there yet
							newCurr = new ArrayList<>(numRooms*15); // assume at most 15 curricula per room
						}
						newCurr.addAll(course.belongsToCurricula);
						kdt_curr_map.put(kdt,newCurr);
					}
				}// end for rooms
				if (t>0) {
					List<Curriculum> curr_now = kdt_curr_map.get(kdt);
					List<Curriculum> curr_previous = kdt_curr_map.get(new KeyDayTime(d,t-1));
					// count one violation for each curriculum in the actual timeslot which was not present in the previous timeslot
					curr_now.stream().forEach(curr -> {
							if (curr_previous.contains(curr) == false) {
								score_soft[0] = score_soft[0]+2;
							}
					});

				}
			} // end for timeslots in a day
		}// end of for: number of days

		//Count points for S2, S3 violations

		//S2
		course_room_Map.entrySet().stream().forEach(entry -> {
			int howManyRooms = entry.getValue().size();
			score_soft[0] = (howManyRooms > 1)? score_soft[0] + howManyRooms -1 : score_soft[0];
		});
		//S3
		course_days_Map.entrySet().stream().forEach( entry -> {
			int howManyDays = entry.getValue().size();
			int diff = howManyDays -  entry.getKey().minWorkDays;
			score_soft[0] = (diff < 0 )?  score_soft[0] - diff*5 : score_soft[0];
		});

	return score_soft[0];
	}

	private Map<KeyDayTimeRoom,Course> applyNBS(Map<KeyDayTimeRoom,Course> tempSol, int whichNBS)
	{
		Map<KeyDayTimeRoom,Course> solution = tempSol;
		Map<KeyDayTime,List<Curriculum>> kdt_banned_Curr_map = new HashMap<>(numDays*timeslotsPerDay);
		Map<KeyDayTime,List<Integer>> kdt_banned_Teach_map = new HashMap<>(numDays*timeslotsPerDay);

		solution.entrySet().stream().filter(entry -> entry.getValue() != null).forEach(entry -> {

			KeyDayTime new_kdt = new KeyDayTime(entry.getKey().getDay(),entry.getKey().getTimeslot());
			List<Curriculum> already_Curr = kdt_banned_Curr_map.get(new_kdt);
			List<Curriculum> new_Curr = new ArrayList<Curriculum>(30);
			if (already_Curr != null) {
				new_Curr.addAll(already_Curr);
			}
			new_Curr.addAll(entry.getValue().belongsToCurricula);
			kdt_banned_Curr_map.put(new_kdt,new_Curr);

			List<Integer> old_teach = kdt_banned_Teach_map.get(new_kdt);
			if (old_teach != null) {
				old_teach.add(entry.getValue().teacherID);
			}
			else {
				old_teach = new ArrayList<Integer>(timeslotsPerDay*numRooms);
				old_teach.add(entry.getValue().teacherID);
			}
			kdt_banned_Teach_map.put(new_kdt, old_teach);
	});


		//mantain feasibility
		switch (whichNBS) {
			case 1:
			{
				// select two events at random and swap timeslots
				SecureRandom rangen = new SecureRandom();
				Boolean done = false;
				Course course_one = null;
				Course course_two = null;
				KeyDayTimeRoom kdtr_one;
				KeyDayTimeRoom kdtr_two;
				KeyDayTime kdt_one;
				KeyDayTime kdt_two;

				while (done != true) {
					//get new two courses until the feasibility condition is satisfied
					do {
						kdtr_one = new KeyDayTimeRoom(rangen.nextInt(numDays), rangen.nextInt(timeslotsPerDay), roomList.get(rangen.nextInt(roomList.size())));
						kdt_one = new KeyDayTime(kdtr_one.getDay(), kdtr_one.getTimeslot());
						kdtr_two = new KeyDayTimeRoom(rangen.nextInt(numDays), rangen.nextInt(timeslotsPerDay), roomList.get(rangen.nextInt(roomList.size())));
						kdt_two = new KeyDayTime(kdtr_two.getDay(), kdtr_two.getTimeslot());
						course_one = solution.get(kdtr_one);
						course_two = solution.get(kdtr_two);
					} while (course_one == null || course_two == null);
					Boolean feasible_one = false;
					Boolean feasible_two = false;

					// insertion kdtr_one to kdtr_two
					// check banned teachers, banned rooms, banned curricula, constraints timeslot, constraints room
					if (course_one.constraintsRoom.contains(kdtr_two.getRoom()) == false) {
						if (course_one.constraintsTimeslot.contains(kdt_two) == false) {
							if (Collections.disjoint(kdt_banned_Curr_map.get(kdt_two), course_one.belongsToCurricula) == true && kdt_banned_Teach_map.get(kdt_two).contains(course_one.teacherID) == false) {
								feasible_one = true;
							}
						}
					}
					// insertion kdtr_two to kdtr_one
					// check banned teachers, banned rooms, banned curricula, constraints timeslot, constraints room
					if (course_two.constraintsRoom.contains(kdtr_one.getRoom()) == false) {
						if (course_two.constraintsTimeslot.contains(kdt_one) == false) {
							if (Collections.disjoint(kdt_banned_Curr_map.get(kdt_one), course_two.belongsToCurricula) == true && kdt_banned_Teach_map.get(kdt_one).contains(course_two.teacherID) == false) {
								feasible_two = true;
							}
						}
					}

					if (feasible_one && feasible_two) {
						done = true;
						//perform insertion
					solution.put(kdtr_one,course_two);
					solution.put(kdtr_two,course_one);
					}
					//Debug
					// System.out.println("Still in NBS1...");
				}
				break;
			}
			case 2:
				
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
		}
		return solution;
	}

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
		// select the entries which may be inserted
		final List<Map.Entry<KeyDayTimeRoom,Course>> insert_entries = parent_a.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_A && entry.getKey().getTimeslot() == crossTimeslot_A).collect(Collectors.toList());


		 List<Course> remove_one_in_child_b = new ArrayList<>(50);

		// insert as many as possible, taking care not to violate any constraints related to the course
		// consider also the other courses which are already present in the timeslot!
		available_entries.forEach(avail -> {
			// get one course which is suitable for the KDTR entry
			List<Curriculum> bannedCurr = new ArrayList<>(100);
			List<Integer> bannedTeacher = new ArrayList<>(100);

			correspondingDay_entries.stream().filter(entry -> entry.getValue() != null).forEach(kdtr -> {
				bannedCurr.addAll(kdtr.getValue().belongsToCurricula);
				bannedTeacher.add(kdtr.getValue().teacherID);
			});

			Course[] CoursetoBeInserted = new Course[1];
			CoursetoBeInserted[0] = null;
			try {
				CoursetoBeInserted[0] = insert_entries.stream().filter(cand -> cand.getValue().constraintsTimeslot.contains(new KeyDayTime(crossDay_B, crossTimeslot_B)) == false && cand.getValue().constraintsRoom.contains(avail.getKey().getRoom()) == false).filter(those -> Collections.disjoint(those.getValue().belongsToCurricula, bannedCurr) && bannedTeacher.contains(those.getValue().teacherID) == false).findAny().get().getValue();
			} catch (Exception ex) { // catch NullPointerExceptions
				CoursetoBeInserted[0] = null;
				//Debug
				System.out.println("From parent a there are no courses which can be inserted under the circumstances");
			}

			if (CoursetoBeInserted[0] != null) {
				child_b.put(avail.getKey(), CoursetoBeInserted[0]);
				//Debug
				System.out.println("Child B -> insert Course with ID " + CoursetoBeInserted[0].courseID);
				//removing the duplicate : due to the insertion above, one lecture must be taken away -> done randomly . for the moment save in list
				remove_one_in_child_b.add(CoursetoBeInserted[0]);
			}

		});// for each avail

		remove_one_in_child_b.stream().forEach( course -> {
			System.out.println(course.courseID + " searching for in B");
			System.out.println("child_b contains the course "+course.courseID + " : " + child_b.containsValue(course));
			//System.out.println(child_b.entrySet().stream().filter(aquellos -> aquellos.getValue().courseID == course.courseID).count());
			});


		/**
		 *  try to insert all events from crossDay_B and crossTimeslot_B from parent B in parent A to produce child A
		 */
		child_a  = parent_a;
		List<Course> remove_one_in_child_a = new ArrayList<>(50);
		available_entries.clear();
		// check first if/how many and which places are even available (course still null) in parent_b in the corresponding day and timeslots
		 final List<Map.Entry<KeyDayTimeRoom,Course>> correspondingDay_entries__ = child_a.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_A && entry.getKey().getTimeslot() == crossTimeslot_A).collect(Collectors.toList());
		 available_entries = correspondingDay_entries__.stream().filter(entry -> entry.getValue() == null).collect(Collectors.toList());

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
					//Debug
					System.out.println("From parent b there are no courses which can be inserted under the circumstances");
				}
			if (CoursetoBeInserted[0] != null) {
				child_a.put(avail.getKey(), CoursetoBeInserted[0]);
				//Debug
				System.out.println("Child A -> insert Course with ID " + CoursetoBeInserted[0].courseID);
				//removing the duplicate : due to the insertion above, one lecture must be taken away -> done randomly . for the moment save in list
				remove_one_in_child_a.add(CoursetoBeInserted[0]);
			}
		});

		remove_one_in_child_a.stream().forEach(course -> {
			System.out.println(course.courseID + " searching for in A");
			System.out.println("child_a contains the course " + course.courseID + " : " + child_a.containsValue(course));
			//System.out.println(child_a.entrySet().stream().filter(aquellos -> aquellos.getValue().courseID == course.courseID).count());
		});

			long crossover_done = System.nanoTime() - crossover_start;
		System.out.println((double) crossover_done / 1000000000.0 + " seconds for the crossover algorithm");
		} // end crossover rate




		/**
		 *
		 *  -------------- MUTATION --------------
		 *
		 */

		// with mutationRate probability there will be a crossover
		
		
		
		
	}

}
