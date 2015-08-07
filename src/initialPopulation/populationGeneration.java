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
	  *  TO DO : initialize all lists below
	  */
	 static List<Curriculum> listCurricula = new ArrayList<>();
	 static List<Course> listCourses = new ArrayList<>();
	 
	 static Map<KeyDayTimeRoom,Course> tempSolution = new HashMap<>();
	 static List<Map<KeyDayTimeRoom,Course>> population = new ArrayList<>();
	 
	 static Map<KeyDayTime,List<Integer/*TeacherID*/>> tabuTimeslot_teacher = new HashMap<>();
	 static Map<KeyDayTime,List<Curriculum>> tabuTimeslot_curriculum = new HashMap<>();
	 
	 

	 
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
		 
	}

}
