/**
 * 
 */
package initialPopulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import definitions.Course;
import definitions.Curriculum;
import definitions.KeyDayTime;
import definitions.KeyDayTimeRoom;
import definitions.Room;

/**
 * @author alexandrubucur
 *
 */

//just a dummy class, delete
class sth{
	Integer one;
	Integer two;
	String three;
	
	public sth(int one, int two, int three){
		this.one = one;
		this.two = two;
		this.three = Integer.toString(three);
	}
}

public class populationGeneration {

	/**
	 * @param args
	 */
	
	 /**
	  * 				!!!!!!!!!!!!!!!!!!!!
	  *  TO DO : initialize all lists below with the right size!!!
	  *   				!!!!!!!!!!!!!!!!!!!!
	  */
	 static List<Curriculum> listCurricula = new ArrayList<>();
	 static List<Course> listCourses = new ArrayList<>();
	 
	 static Map<KeyDayTimeRoom,Course> tempSolution = new HashMap<>();
	 static List<Map<KeyDayTimeRoom,Course>> population = new ArrayList<>();
	 
	 static Map<KeyDayTime,List<Integer/*TeacherID*/>> tabuTimeslot_teacher = new HashMap<>();
	 static Map<KeyDayTime,List<Curriculum>> tabuTimeslot_curriculum = new HashMap<>();
	 
	 static Map<KeyDayTimeRoom, Double> timeslotRoom_g_jk_map = new HashMap<>();
	 static Map<Course, List<Room>> course_rooms_map = new HashMap<>();
	 static Map<Course, List<Integer>> course_day_map = new HashMap<>();
     //static Map<KeyDayTime, List<Curriculum>> soft_const_4_list = new HashMap<>(); 
	 
	 static Integer aps = 0;
	 static Integer apd = 0;
	 static List<KeyDayTime> checkedKDT = new ArrayList<>();
	 
	private static Integer[] calc_apd_aps (Course course_i, Map<KeyDayTimeRoom,Course> tempSol) {
		// apd = return the total number of available periods for course_i under the temporary solution tempSol
		// aps = return the total number of available period-room positions for course_i under the temporary solution tempSol
		
		aps = 0;
		apd = 0;
		// from the entries where the course has not yet been set, return those where the teacher and the curricula to which the course belongs are not banned from the timeslots
		tempSol.entrySet().stream().filter(entry -> entry.getValue().equals(null)).forEach((entry)-> {
			int Day = entry.getKey().getDay();
			int Timeslot = entry.getKey().getTimeslot();
			KeyDayTime kdt = new KeyDayTime(Day,Timeslot);
			if (tabuTimeslot_teacher.get(kdt).contains(course_i.TeacherID) == false) {
				//teacher has not been banned yet, check if curricula bans for timeslot contain any curriculum to which the course belongs.
				if (Collections.disjoint(tabuTimeslot_curriculum.get(kdt), course_i.belongsToCurricula)) {
					// teacher not banned, and no curricula to which the course belongs are banned during the timeslot, course_i could be assigned
					aps +=1;
					if (checkedKDT.contains(kdt) == false) {
						//if timeslot not already checked for apd
						apd+=1;
						checkedKDT.add(kdt);
					}
				}
			}
		});
		Integer[] apd_aps = {apd,aps};
		return apd_aps;
	}
	
	public static  List<Curriculum> currForCourse(Course course_i) {
		List<Curriculum> belongsToCurr = new ArrayList<>();
		listCurricula.stream().forEach((curr)-> {
			if (curr.coursesThatBelongToCurr.contains(course_i)){
			belongsToCurr.add(curr);
			}
		});
		return belongsToCurr;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		/**
		 *  try to populate the HashMap with some dummy values
		 */
		 Map<KeyDayTimeRoom,String> myMap = new HashMap<>();
		 myMap.put(new KeyDayTimeRoom(0,0,new Room(1,156)), "first");
		 myMap.put(new KeyDayTimeRoom(15,23,new Room(4,455)), "second");
		 
		 myMap.keySet().stream().filter( kdtr -> kdtr.getRoom().getRoomCapacity() == 455).forEach(kdtr -> System.out.println(kdtr.getDay()));
		 
		 Map<KeyDayTimeRoom,sth> myObjMap = new HashMap<>();
		 myObjMap.put(new KeyDayTimeRoom(0,0,new Room(1,156)), new sth(1,2,3));
		 myObjMap.put(new KeyDayTimeRoom(15,23,new Room(4,455)), new sth(5,234,32));
		 
		 myObjMap.values().stream().filter(stuff -> stuff.three.equals("32")).forEach(stuff -> System.out.println(stuff.three));
		 
		 /**
		  *  TO DO : initialize the lists with courses, curricula, rooms,...
		  */

		 
		 /**
		  *  Generate the population population
		  */
		 
		 /***************** Choose a course according to HR 1 *****************/
		 /***************** courses with small numbers of available periods and large number of unassigned lectures have priority *****************/
		 
		 Map<Course,Double> course_apd_i = new HashMap<>();
		 Map<Course,Double> course_aps_i = new HashMap<>();
		 double min_apd = 0;
		 double min_aps = 0;
		 double max = 0;
		 Course selectedCourse = null;
		 // fill the maps with the courses and their corresponding apd and aps divided by the number of unassigned lectures
		 listCourses.stream().forEach((course) -> {
		 Integer[] apd_aps = new Integer[2];
	     apd_aps = calc_apd_aps(course,tempSolution);
		 double unassignedLectures = course.numberOfUnassignedLectures;
		 course_apd_i.putIfAbsent(course, apd_aps[0]/Math.sqrt(unassignedLectures));
		 course_aps_i.putIfAbsent(course, apd_aps[1]/Math.sqrt(unassignedLectures));
		 });		 
		 
		// choose the course with the smallest value of apd_i(X)/sqrt(nl_i(X))
		 
		// find minimum for apd first
		 min_apd = 1000;
		 min_aps = 1000;
		 max = 0;
		 for(Entry<Course, Double> entry : course_apd_i.entrySet()) {
			 min_apd = entry.getValue() - min_apd >= 0.01 ?  min_apd : entry.getValue();
			}
		 
		// add all elements that have a value equal to min_apd
		 List<Course> tieCourses_apd = new ArrayList<>();
		 List<Course> tieCourses_aps = new ArrayList<>();
		 for(Entry<Course, Double> entry : course_apd_i.entrySet()) {
			 if (entry.getValue() - min_apd <= 0.01 ) {
				 tieCourses_apd.add(entry.getKey());
			 }
		 }
		 
		// if tie between courses, choose the course with smallest value of aps_i(X)/sqrt(nl_i(X))
		 if (tieCourses_apd.size() >1 ) {
			// find minimum for aps first
			 min_aps = 1000;
			 for(Entry<Course, Double> entry : course_aps_i.entrySet()) {
				 min_aps = entry.getValue() - min_aps >= 0.01 ?  min_aps : entry.getValue();
				}
			 
			// add all elements that have a value equal to min_aps
			 for(Entry<Course, Double> entry : course_aps_i.entrySet()) {
				 if (entry.getValue() - min_aps <= 0.01 ) {
					 tieCourses_aps.add(entry.getKey());
				 }
			 }
			 
			 if (tieCourses_aps.size()>1){
				 // still courses with ties even for aps, now decide based on number of courses that share common students or teacher with course c_i
				 // that means, the course with max : curricula to which it belongs x courses in curricula
				 
				 Map<Course,Integer> con_f_i  = new HashMap<>();
				 tieCourses_aps.stream().forEach((course) -> {
					 
					 con_f_i.putIfAbsent(course, 0);
					 currForCourse(course).forEach((curr) -> {
						 //courses that share common students -> courses in curricula in which the course is also present
						con_f_i.put(course, con_f_i.get(course) + curr.coursesThatBelongToCurr.size()); 
					 });
					 //courses that share a common teacher
					   con_f_i.put(course, (int) (con_f_i.get(course) + listCourses.stream().filter( (kurs) -> kurs.TeacherID == course.TeacherID).count()));
				 });
				 
					// find maximum for con_f_i first
				 
				 for(Entry<Course, Integer> entry : con_f_i.entrySet()) {
					 max = entry.getValue() - min_aps >= 0.01 ? entry.getValue() : max;
					}
				 
				 for(Entry<Course, Integer> entry_ : con_f_i.entrySet()) {
					 if (entry_.getValue() - max <= 0.01 ) {
						 selectedCourse = entry_.getKey();
						 break;
					 }
				 }
				 
			 }
			 else{
				 //only one element with smallest aps_i, select course
				 selectedCourse = tieCourses_aps.get(0);
			 }
		 }
		 else {
			 // only one element with smallest apd_i, select course
			 selectedCourse = tieCourses_apd.get(0);
		 }
		 
		 /***************** Assign the course according to HR 2 *****************/
		 /***************** select a period among all available ones that is least likely to be used by other unfinished courses at later steps *****************/
		 
		 Course[] chosenCourse = new Course[1];
		 chosenCourse[0] = selectedCourse;
		 
		 // for each available period-room pair choose the pair with the smallest value of g(j,k) = k_1 * uac_i_j(X) + k_2 * Delta_f_s(i,j,k)
		 tempSolution.entrySet().stream().filter( (kdtr) -> kdtr.getValue().equals(null)).forEach( (entry) -> {
			    //check for feasibility of entry, where teacher or curriculum banned
			    int Day = entry.getKey().getDay();
				int Timeslot = entry.getKey().getTimeslot();
				KeyDayTime kdt = new KeyDayTime(Day,Timeslot);
				if (tabuTimeslot_teacher.get(kdt).contains(chosenCourse[0].TeacherID) == false) {
					//teacher has not been banned yet, check if curricula bans for timeslot contain any curriculum to which the course belongs.
					if (Collections.disjoint(tabuTimeslot_curriculum.get(kdt), chosenCourse[0].belongsToCurricula)) {
						//feasible insertion, check value of g(j,k) and add it to map (don't assign the course, just add to map kdtr -> g(j,k))
						
						int[] uac_ij = new int[1];
						uac_ij[0] = 0;
						//count courses with same teacher
						uac_ij[0] = (int) listCourses.stream().filter(curso -> curso.TeacherID == chosenCourse[0].TeacherID).count();
						// count courses from same curricula * number of unfinished lectures of that course
						chosenCourse[0].belongsToCurricula.forEach( curriculum -> {
							curriculum.coursesThatBelongToCurr.forEach(course_in_curr -> {
								uac_ij[0] += course_in_curr.numberOfUnassignedLectures;
							});
						});
						
						//calculate soft penalties
						int[] soft_penalty = new int[1];
						soft_penalty[0] = 0;
						
						//S1 enrolled students - room capacity  -> konstante a_1 = 1
					    int[] enrolledStudents = new int[1];
						enrolledStudents[0] = 0;
						
						chosenCourse[0].belongsToCurricula.forEach( curr -> {
							enrolledStudents[0] += curr.numberOfStudents;
						});
						if (enrolledStudents[0] > entry.getKey().getRoom().getRoomCapacity()) {
							soft_penalty[0] += 1 * (enrolledStudents[0] - entry.getKey().getRoom().getRoomCapacity());
						}
						
						//S2 room stability -> konstante a_2 = 1
						soft_penalty[0] += (course_rooms_map.entrySet().stream().count() - 1);
						
						//S3 minimum working days -> konstante a_3 = 5
						if (course_day_map.get(chosenCourse[0]).size() < chosenCourse[0].minWorkDays) {
						soft_penalty[0] += 5 * (course_day_map.get(chosenCourse[0]).size() - chosenCourse[0].minWorkDays);
						}
						
						//S4 curriculum compactness -> konstante a_4 = 2
						
						//check only previous timeslot, all other ones are the same
						Map<Integer, List<Curriculum>> timeSlot_Curricula = new HashMap<>();
						
						if (Timeslot>1 ){
					    tempSolution.entrySet().stream().filter(eintrag -> eintrag.getKey().Day == Day && eintrag.getKey().Timeslot == Timeslot - 1).forEach( timeRoom -> {
					    	List<Curriculum> listToAdd = timeSlot_Curricula.get(Timeslot-1);
					    	listToAdd.addAll(timeRoom.getValue().belongsToCurricula);
					    	timeSlot_Curricula.put(Timeslot-1, listToAdd);
					    });
					    tempSolution.entrySet().stream().filter(eintrag -> eintrag.getKey().Day == Day && eintrag.getKey().Timeslot == Timeslot).forEach( timeRoom -> {
					    	List<Curriculum> listToAdd = timeSlot_Curricula.get(Timeslot);
					    	listToAdd.addAll(timeRoom.getValue().belongsToCurricula);
					    	listToAdd.addAll(chosenCourse[0].belongsToCurricula);
					    	timeSlot_Curricula.put(Timeslot, listToAdd);
					    });
					    
					    //every curriculum in the new timeslot which is not present in the previous one is one violation
					    timeSlot_Curricula.get(new KeyDayTime(Day,Timeslot)).stream().forEach(curr -> {
					    	if (timeSlot_Curricula.get(new KeyDayTime(Day,Timeslot)).contains(curr) == false) {
					    		soft_penalty[0] += 1;
					    	}
					    });
						}
						
						//calculate g_j_k
						timeslotRoom_g_jk_map.put(entry.getKey(), uac_ij[0] + 0.5* soft_penalty[0]);
					}
				}	
		 });
		 
	}//main
}
