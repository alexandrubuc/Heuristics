package memeticAlgo;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Queue;
import java.util.LinkedList;
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
	 public static Map<KeyDayTimeRoom,Course> grandchild_a = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> grandchild_b = new HashMap<>(1000);
	 public static Map<KeyDayTimeRoom,Course> S_best = new HashMap<>(1000);
	 public static List<Map<KeyDayTimeRoom,Course>> population = new ArrayList<>(50);
	 public static double crossoverRate = 0.8;
	 public static double mutationRate = 0.04;
	 public static int numDays;
	 public static int timeslotsPerDay;
	 public static int popSize;
	 public static int numCourses;
	 public static int numRooms;
	 public static int numCurricula;
     public static List<Room> roomList = new ArrayList<>(100);
	 public static List<Curriculum> CurriculumList = new ArrayList<>(200);


	public static int calcPenaltyScore(Map<KeyDayTimeRoom,Course> tempSol) {
		int hardScore = calcPenaltyScore_HARD(tempSol);
		int softScore = calcPenaltyScore_SOFT(tempSol);
		return hardScore + softScore;
	}

	public static int calcPenaltyScore_HARD(Map<KeyDayTimeRoom,Course> tempSol) {
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

	public static int calcPenaltyScore_SOFT(Map<KeyDayTimeRoom,Course> tempSol) {

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
						List<Room> rooms = new ArrayList<>(numRooms);
						List<Room> already_there = course_room_Map.get(course);
						if (already_there != null) {
							//add to list
							rooms.addAll(already_there);
						}
						rooms.add(room);
						rooms = Funktionen.removeDuplicatesRoom(rooms);
						course_room_Map.put(course,rooms);


						//S3: Minimum working days: lectures must be spread over a min of working days
						// constant is 5

						List<Integer> days = new ArrayList<>(numDays);
						List<Integer> days_there = course_days_Map.get(course);
						if (days_there != null) {
							//add to list
							days.addAll(days_there);
						}
						days.add(d);
						days = Funktionen.removeDuplicatesInteger(days);
						course_days_Map.put(course,days);

						//S4: Curriculum compactness: violation counted if there is one lecture not adjacent to any other lecture belonging to the same curriculum within the same day
						//constant is 2

						List<Curriculum> old = kdt_curr_map.get(kdt);
						List<Curriculum> newCurr = new ArrayList<>(numRooms*15); // assume at most 15 curricula per room
						if (old != null) {
							//add to list
							newCurr.addAll(old);
						}
						newCurr.addAll(course.belongsToCurricula);
						kdt_curr_map.put(kdt,newCurr);
					}
				}// end for rooms
				if (t>0) {
					List<Curriculum> curr_now = kdt_curr_map.get(kdt);
					List<Curriculum> curr_previous = kdt_curr_map.get(new KeyDayTime(d,t-1));
					// count one violation for each curriculum in the actual timeslot which was not present in the previous timeslot
					// NPE : consider possibility of them being null
					if (curr_previous != null) {
						if (curr_now != null) {
							curr_now.stream().forEach(curr -> {
								if (curr_previous.contains(curr) == false) {
									score_soft[0] = score_soft[0] + 2;
								}
							});
						}
						}
					else if (curr_previous == null) {
						if (curr_now != null) {
							curr_now.stream().forEach(curr -> {
									score_soft[0] = score_soft[0] + 2;
							});
						}
					}
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

	private static Map<KeyDayTimeRoom,Course> applyNBS(Map<KeyDayTimeRoom,Course> tempSol, int whichNBS)
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


		SecureRandom rangen = new SecureRandom();
		Boolean done = false;
		Course course_one = null;
		Course course_two = null;
		KeyDayTimeRoom kdtr_one;
		KeyDayTimeRoom kdtr_two;
		KeyDayTime kdt_one;
		KeyDayTime kdt_two;


		//mantain feasibility
		switch (whichNBS) {
			case 1:
			{
				// select two events at random and swap timeslots
				int r = 0;
				while (done != true && r<50) {
					r=r+1;
					//get new two courses until the feasibility condition is satisfied
					do {
						//Debug
						//System.out.println("Shiiiiiiit NBS1");

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
					// System.out.println("Still in NBS1 -- " + r);
				}
				break;
			}
			case 2:
				//select one event at random and move it to a feasible timeslot
				Boolean passendesGefunden = false;
				int i=0;
					do {
						i=i+1;
						do {
							kdtr_one = new KeyDayTimeRoom(rangen.nextInt(numDays), rangen.nextInt(timeslotsPerDay), roomList.get(rangen.nextInt(roomList.size())));
							//Debug
							//System.out.println("Shiiiiiiit NBS2");
						} while (solution.get(kdtr_one) == null);
						//kdtr_one = new KeyDayTimeRoom(rangen.nextInt(numDays), rangen.nextInt(timeslotsPerDay), roomList.get(rangen.nextInt(roomList.size())));
						kdt_one = new KeyDayTime(kdtr_one.getDay(), kdtr_one.getTimeslot());
						final Course course_insert = solution.get(kdtr_one);
						List<Map.Entry<KeyDayTimeRoom,Course>> null_list= solution.entrySet().stream().filter(entry -> entry.getValue() == null).collect(Collectors.toList());
						if (null_list != null && null_list.size() > 0) {

							outerloop: for (int n=0;n<null_list.size() ; n++) {
								Map.Entry<KeyDayTimeRoom, Course> entry = null_list.get(n);
								KeyDayTime kdt_null = new KeyDayTime(entry.getKey().getDay(), entry.getKey().getTimeslot());
								KeyDayTimeRoom kdtr_null = entry.getKey();
								//checked for banned curricula and banned teachers
								//Debug
								//System.out.println(kdt_banned_Curr_map.get(kdt_null).size());
								//System.out.println(course_insert.belongsToCurricula);
								//System.out.println(kdt_banned_Teach_map.get(kdt_null).size());


								if (kdt_banned_Curr_map.get(kdt_null) == null || Collections.disjoint(kdt_banned_Curr_map.get(kdt_null), course_insert.belongsToCurricula)) {
									if (kdt_banned_Teach_map.get(kdt_null) == null || kdt_banned_Teach_map.get(kdt_null).contains(course_insert.teacherID) == false) {
										//check for course constraints
										if (course_insert.constraintsRoom.contains(kdtr_null.getRoom()) == false) {
											if (course_insert.constraintsTimeslot.contains(kdt_null) == false) {
												//possible insertion
												solution.put(kdtr_null, course_insert);
												// put null where the course was previously
												solution.put(kdtr_one, null);
												//break the for each loop
												passendesGefunden = true;
												break outerloop;
											}
										}
									}
								}
							}
						}
						//Debug
						//System.out.println("Still in NBS2 -- " + i);
					} while (i<50 &&  passendesGefunden == false);


				break;
				
			case 3:
				// select two timeslots at random and swap all events
				//first check feasibility
				done = false;
				Boolean erfolg_one = true;
				Boolean erfolg_two = true;
				do {
					//Debug
					//System.out.println("Shiiiiiiit NBS3");
					kdt_one = new KeyDayTime(rangen.nextInt(numDays), rangen.nextInt(timeslotsPerDay));
					kdt_two = new KeyDayTime(rangen.nextInt(numDays), rangen.nextInt(timeslotsPerDay));
				} while(kdt_one == kdt_two);

				int j = 0;
				do {
					j=j+1;
					erfolg_one = true;
					erfolg_two = true;

					outer:for(int s=0;s<solution.size();s++) {
						for (int w=0;w<numRooms;w++) {
							Course course_ = solution.get(new KeyDayTimeRoom(kdt_one.getDay(),kdt_one.getTimeslot(),roomList.get(w)));
							if (course_ != null) {
								// if the course is not zero, check for timeslot constraints regarding kdt_two
								if (course_.constraintsTimeslot.contains(kdt_two)) {
									erfolg_one = false;
									break outer;
								}
							}
						}
					}

					//try with the second one only if erfolg_one = true
					if (erfolg_one == true) {
						outer:for(int s=0;s<solution.size();s++) {
							for (int w=0;w<numRooms;w++) {
								Course course_ = solution.get(new KeyDayTimeRoom(kdt_two.getDay(),kdt_two.getTimeslot(),roomList.get(w)));
								if (course_ != null) {
									// if the course is not zero, check for timeslot constraints regarding kdt_one
									if (course_.constraintsTimeslot.contains(kdt_one)) {
										erfolg_two = false;
										break outer;
									}
								}
							}
						}
					}

					if (erfolg_one && erfolg_two) {
						done = true;
						//perform swap of courses
						for (int e=0; e<numRooms; e++) {
							KeyDayTimeRoom kdtr_first = new KeyDayTimeRoom(kdt_one.getDay(),kdt_one.getTimeslot(),roomList.get(e));
							KeyDayTimeRoom kdtr_second = new KeyDayTimeRoom(kdt_two.getDay(),kdt_two.getTimeslot(),roomList.get(e));
							Course zwischenSpeicher = null;
							zwischenSpeicher = solution.get(kdtr_first);
							solution.put(kdtr_first,solution.get(kdtr_second));
							solution.put(kdtr_second,zwischenSpeicher);
						}
					}
					//Debug
					//System.out.println("still stuck in NBS 3 -- " + j);
				} while (done == false && j<50); // wir versuchen es bis es klappt, aber maximal 50 mal. Achtung keine Verhinderung der Wiederholung mit den gleichen timeslots.

				break;
			case 4:
				break;
			case 5:
				break;
			case 6:
				break;
			case 7:
				break;
			case 8:
				break;
			case 9:
				break;
			default:
				break;
		}
		return solution;
	}


	/**
	 *
	 *
	 *
	 * ************************************* MAIN ***********************************
	 *
	 *
	 *
	 */




	public static void main(String[] args) {

		//Debug
		//System.out.println("You just entered the world of memeticAlgo!!");
		/**
		 * Call the main method of the populationGeneration class so as so generate the starting population == the parents
		 * have the main method copy the population into local population here, numDays and timeslotsPerDay
		 */
		long start_time = System.nanoTime();


		String[] useless_args = new String[1];
		useless_args = null;
		initialPopulation.populationGeneration.main_(useless_args);

		//may need to change it
		int tabuSize = 7;

		/**
		 * choose a best solution from the population, S_best
		 */

		Map<Map<KeyDayTimeRoom,Course>,Integer> solution_score_map = new HashMap<>(popSize+4); // popsize + 4 because of the 4 children and grandchildren who come after each round, before removing the weakest
		population.stream().forEach(solu -> solution_score_map.put(solu, calcPenaltyScore(solu)));


		//Debug
		/*System.out.println("popSize "+popSize);
		System.out.println("populationSize " +population.size());
		System.out.println("solution_score_map "+solution_score_map.size()); // only 1 inside?
		//since only one inside of solution_score_map, check if in population always the same map
		population.forEach(entry -> System.out.println("HashCode of entry "+  +entry.hashCode()));
		for(int g=1;g<population.size(); g++) {
			if (population.get(g).equals(population.get(g-1))) {
				System.out.println("Solution "+g+" equals "+ (g-1));
			}
			else {
				System.out.println("The solutions are not all the same!!!");
			}
		}
*/


		/**
		 * get solution with minimum score
		 */
		final Integer[] S_best_Min = new Integer[1];
		S_best_Min[0] = Collections.min(solution_score_map.values());

		S_best =	solution_score_map.entrySet().stream().filter(entry -> entry.getValue() == S_best_Min[0]).findAny().get().getKey();
		//Debug
		System.out.println("The score of S_best solution is "+ Integer.toString(S_best_Min[0]));

		/**
		 * create empty tabu list with TabuSize, T_list, where the Neighbourhood Structures will be stored
		 */
		Queue<Integer> TabuList = new LinkedList<Integer>(); //always use .remove due to the implementation of the FIFO interface
		Boolean CanSelect = true;


		long endTime;
		do {

			if (TabuList != null && TabuList.size() > 1) {
				TabuList.remove();
			}


			/**
			 *
			 *
			 *
			 *  -------------- CROSSOVER --------------
			 *
			 *
			 *
			 */

			// with crossoverRate probability there will be a crossover
			SecureRandom rangen = new SecureRandom();

			if (rangen.nextInt(100) < crossoverRate*100) {
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
				child_b = parent_b;
				// check first if/how many and which places are even available (course still null) in parent_b in the corresponding day and timeslots
				final List<Map.Entry<KeyDayTimeRoom, Course>> correspondingDay_entries = child_b.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_B && entry.getKey().getTimeslot() == crossTimeslot_B).collect(Collectors.toList());
				List<Map.Entry<KeyDayTimeRoom, Course>> available_entries = correspondingDay_entries.stream().filter(entry -> entry.getValue() == null).collect(Collectors.toList());
				// select the entries which may be inserted
				final List<Map.Entry<KeyDayTimeRoom, Course>> insert_entries = parent_a.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_A && entry.getKey().getTimeslot() == crossTimeslot_A).collect(Collectors.toList());


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
						//System.out.println("From parent a there are no courses which can be inserted under the circumstances");
					}

					if (CoursetoBeInserted[0] != null) {
						child_b.put(avail.getKey(), CoursetoBeInserted[0]);
						//Debug
						//	System.out.println("Child B -> insert Course with ID " + CoursetoBeInserted[0].courseID);
						//removing the duplicate : due to the insertion above, one lecture must be taken away -> done randomly . for the moment save in list
						remove_one_in_child_b.add(CoursetoBeInserted[0]);
					}

				});// for each avail

				// remove the duplicates
				for (int i = 0; i < remove_one_in_child_b.size(); i++) {
					Course course = remove_one_in_child_b.get(i);
					//Debug
					//	System.out.println(course.courseID + " searching for in B");
					//	System.out.println("child_b contains the course "+course.courseID + " : " + child_b.containsValue(course));

					days:
					for (int d = 0; d < numDays; d++) {
						for (int t = 0; t < timeslotsPerDay; t++) {
							for (int r = 0; r < numRooms; r++) {
								KeyDayTimeRoom kdtro = new KeyDayTimeRoom(d, t, roomList.get(r));
								Course resultat = child_b.get(kdtro);
								if (resultat != null && resultat.equals(course)) {
									//put null and break loop
									child_b.put(kdtro, null);
									break days;
								}
							}
						}
					}
				}


				/**
				 *  try to insert all events from crossDay_B and crossTimeslot_B from parent B in parent A to produce child A
				 */
				child_a = parent_a;
				List<Course> remove_one_in_child_a = new ArrayList<>(50);
				available_entries.clear();
				// check first if/how many and which places are even available (course still null) in parent_b in the corresponding day and timeslots
				final List<Map.Entry<KeyDayTimeRoom, Course>> correspondingDay_entries__ = child_a.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_A && entry.getKey().getTimeslot() == crossTimeslot_A).collect(Collectors.toList());
				available_entries = correspondingDay_entries__.stream().filter(entry -> entry.getValue() == null).collect(Collectors.toList());

				// select the entries which may be inserted
				final List<Map.Entry<KeyDayTimeRoom, Course>> insert_entries__ = parent_b.entrySet().stream().filter(entry -> entry.getKey().getDay() == crossDay_B && entry.getKey().getTimeslot() == crossTimeslot_B).collect(Collectors.toList());

				// insert as many as possible, taking care not to violate any constraints related to the course
				// consider also the other courses which are already present in the timeslot!
				available_entries.forEach(avail -> {
					// get one course which is suitable for the KDTR entry

					List<Curriculum> bannedCurr = new ArrayList<>(100);
					List<Integer> bannedTeacher = new ArrayList<>(100);

					correspondingDay_entries__.stream().filter(entry -> entry.getValue() != null).forEach(kdtr -> {
						bannedCurr.addAll(kdtr.getValue().belongsToCurricula);
						bannedTeacher.add(kdtr.getValue().teacherID);
					});

					Course[] CoursetoBeInserted = new Course[1];
					try {
						CoursetoBeInserted[0] = insert_entries__.stream().filter(cand -> cand.getValue().constraintsTimeslot.contains(new KeyDayTime(crossDay_A, crossTimeslot_A)) == false && cand.getValue().constraintsRoom.contains(avail.getKey().getRoom()) == false).filter(those -> Collections.disjoint(those.getValue().belongsToCurricula, bannedCurr) && bannedTeacher.contains(those.getValue().teacherID) == false).findAny().get().getValue();
					} catch (Exception ex) { // catch NullPointerExceptions
						CoursetoBeInserted[0] = null;
						//Debug
						//System.out.println("From parent b there are no courses which can be inserted under the circumstances");
					}
					if (CoursetoBeInserted[0] != null) {
						child_a.put(avail.getKey(), CoursetoBeInserted[0]);
						//Debug
						//System.out.println("Child A -> insert Course with ID " + CoursetoBeInserted[0].courseID);
						//removing the duplicate : due to the insertion above, one lecture must be taken away -> done randomly . for the moment save in list
						remove_one_in_child_a.add(CoursetoBeInserted[0]);
					}
				});

				// remove the duplicates
				for (int i = 0; i < remove_one_in_child_a.size(); i++) {
					Course course = remove_one_in_child_a.get(i);
					//Debug
					//System.out.println(course.courseID + " searching for in A");
					//System.out.println("child_a contains the course "+course.courseID + " : " + child_a.containsValue(course));

					days:
					for (int d = 0; d < numDays; d++) {
						for (int t = 0; t < timeslotsPerDay; t++) {
							for (int r = 0; r < numRooms; r++) {
								KeyDayTimeRoom kdtro = new KeyDayTimeRoom(d, t, roomList.get(r));
								Course resultat = child_a.get(kdtro);
								if (resultat != null && resultat.equals(course)) {
									//put null and break loop
									child_a.put(kdtro, null);
									break days;
								}
							}
						}
					}
				}

				long crossover_done = System.nanoTime() - crossover_start;
				//System.out.println((double) crossover_done / 1000000000.0 + " seconds for the crossover algorithm");
			} // end crossover rate



			int whichNbs;
			/**
			 *
			 *  -------------- MUTATION --------------
			 *
			 */

			// with mutationRate probability there will be a crossover
			if (rangen.nextInt(100) < mutationRate*100) {
				/**
				 * apply mutation operator to the children and accept the solution regardless of its quality
				 */
				whichNbs = 1 + rangen.nextInt(9);
				child_b = applyNBS(child_b,whichNbs);
				whichNbs = 1 + rangen.nextInt(9);
				child_a = applyNBS(child_a,whichNbs);
			}

			/**
			 *
			 *
			 *  --------------  TABU LIST MUTATION --------------
			 *
			 *
			 */

			/**
			 * select a NBS which is not in the tabu list
			 */
			do {
				whichNbs = 1 + rangen.nextInt(9);
				//Debug
				//System.out.println("Stuck trying to choose the Nbs to use..");
			} while (TabuList.contains(whichNbs));

			/**
			 * apply whichNbs to the children to produce the grandchildren
			 */
			grandchild_a = applyNBS(child_a, whichNbs);
			grandchild_b = applyNBS(child_b, whichNbs);

			/**
			 * obtain minimum penalty solution from the children and grandchildren
			 */

			Map<Map<KeyDayTimeRoom,Course>,Integer> offspring_score_map = new HashMap<>(4);
			int score_child_a = calcPenaltyScore(child_a);
			int score_child_b = calcPenaltyScore(child_b);
			int score_grandchild_a = calcPenaltyScore(grandchild_a);
			int score_grandchild_b = calcPenaltyScore(grandchild_b);

			offspring_score_map.put(child_a,score_child_a);
			offspring_score_map.put(child_a,score_child_b);
			offspring_score_map.put(child_a, score_grandchild_a);
			offspring_score_map.put(child_a, score_grandchild_b);

			int mini = Collections.min(offspring_score_map.values());
			Map<KeyDayTimeRoom,Course> candidate = offspring_score_map.entrySet().stream().filter(entry -> entry.getValue() == mini).findAny().get().getKey();

			/**
			 * if the obtained solution is better than the old S_best, then make it to S_best
			 */
			if (mini < S_best_Min[0]) {
				S_best = candidate;
				S_best_Min[0] = mini;
				CanSelect = false;
			}
			else {
				CanSelect = true;
				TabuList.add(whichNbs);
			}

			/**
			 * Update the population by inserting and removing the best and worst solutions, while mantaining the size of the population
			 * do it efficiently without calculating all scores again!
			 */

		//solution_score_map
			solution_score_map.put(child_a,score_child_a);
			solution_score_map.put(child_b,score_child_b);
			solution_score_map.put(grandchild_a,score_grandchild_a);
			solution_score_map.put(grandchild_b,score_grandchild_b);
		//now remove the four weakest
			do {
				Integer lowest_score = Collections.min(solution_score_map.values());
				//potential nullpointer exception if no key is found for the lowest score
				Map<KeyDayTimeRoom,Course> key_lowest_score = solution_score_map.entrySet().stream().filter(entry -> entry.getValue() == lowest_score).findAny().get().getKey();
				solution_score_map.remove(key_lowest_score,lowest_score);
				//Debug
				//System.out.println("Removing weakest solution in ");
			} while (solution_score_map.size() > popSize);

			 endTime = System.nanoTime();
			//Debug
		//	System.out.println("Runtime : " + (endTime - start_time) / 1000000000.0 + " seconds" );
		} while (endTime - start_time < 100*1000000000.0 ); //end of do while

		// Debug - See Timetable and its score!
		/*System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------");
		System.out.println("-------------------------------------------------------------------------------\n\n\n");
		System.out.print("The score of the winning solution after 100 seconds is : " + Integer.toString(calcPenaltyScore(S_best)) );
	     roomList.stream().forEach(roomie -> {
			 String RoomieID = roomie.getRoomID();
			 while (RoomieID.chars().count() < 5) {
				 RoomieID += " ";
			 }
			 System.out.print("\n\nRoom " + RoomieID + "    ");
			 for (int r = 0; r < numDays; r++) {
				 for (int e = 0; e < timeslotsPerDay; e++) {
					 String cursoID = "Free";
					 Course curso = S_best.get(new KeyDayTimeRoom(r, e, roomie));
					 if (curso != null) {
						 cursoID = curso.courseID.toString();
						 while (cursoID.chars().count() < 4) {
							 cursoID += " ";
						 }
					 }
					 System.out.print(cursoID + "    ");
				 }
		}
		}); // end roomie */


	}

}
