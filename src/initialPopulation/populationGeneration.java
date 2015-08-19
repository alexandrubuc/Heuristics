/**
 * 
 */
package initialPopulation;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.text.DecimalFormat;
import definitions.Course;
import definitions.Curriculum;
import definitions.KeyDayTime;
import definitions.KeyDayTimeRoom;
import definitions.Room;
import hilfsFunktionen.Funktionen;
import memeticAlgo.Algorithm;
import xmlParser.Parser;



/**
 * @author alexandrubucur
 *
 */

public class populationGeneration {

	/**
	 * @param args
	 */
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";



	 public static List<Curriculum> listCurricula = new ArrayList<>(100);
	 public static List<Course> listCourses = new ArrayList<>(100);
	 public static List<Room> listRooms = new ArrayList<>(50);
	 public static int totalDays;
	 public static int timeslotsPerDay;
	 public static int dailyLecturesMin;
	 public static int dailyLecturesMax;
	 public static int popSize = 1;
	 public static int numCourses;

	 public static Map<KeyDayTimeRoom,Course> tempSolution = new HashMap<>(1000);
	 static List<Map<KeyDayTimeRoom,Course>> population = new ArrayList<>(50);
	 
	 static Map<KeyDayTime,List<Integer/*TeacherID*/>> tabuTimeslot_teacher = new HashMap<>(100);
	 static Map<KeyDayTime,List<Curriculum>> tabuTimeslot_curriculum = new HashMap<>(100);
	 
	 static Map<KeyDayTimeRoom, Double> timeslotRoom_g_jk_map = new HashMap<>(1000);
	 static Map<Course, List<Room>> course_rooms_map = new HashMap<>(100);
	 static Map<Course, List<Integer>> course_day_map = new HashMap<>(100);
     //static Map<KeyDayTime, List<Curriculum>> soft_const_4_list = new HashMap<>(); 
	 
	 static int aps = 0;
	 static int apd = 0;
	 static List<KeyDayTime> checkedKDT = new ArrayList<>();


	private static int[] calc_apd_aps (Course course_i, Map<KeyDayTimeRoom,Course> tempSol) {
		// apd = return the total number of available periods for course_i under the temporary solution tempSol
		// aps = return the total number of available period-room positions for course_i under the temporary solution tempSol
        checkedKDT.clear();
		aps = 0;
		apd = 0;
		// from the entries where the course has not yet been set, return those where the teacher and the curricula to which the course belongs are not banned from the timeslots
		//System.out.println("tempSoll entries still with value null : "+ tempSol.entrySet().stream().filter(entry -> entry.getValue() == null).count());
		tempSol.entrySet().stream().filter(entry -> entry.getValue() == null).filter(entry -> (course_i.constraintsRoom.contains(entry.getKey().assignedRoom) == false) && (course_i.constraintsTimeslot.contains(new KeyDayTime(entry.getKey().Day,entry.getKey().Timeslot))==false)).forEach((entry)-> {
			int Day = entry.getKey().getDay();
			int Timeslot = entry.getKey().getTimeslot();
			KeyDayTime kdt = new KeyDayTime(Day,Timeslot);
			if ((tabuTimeslot_teacher.get(kdt) != null && tabuTimeslot_teacher.get(kdt).contains(course_i.teacherID) == false) || tabuTimeslot_teacher.get(kdt)  == null) {
				//teacher has not been banned yet, check if curricula bans for timeslot contain any curriculum to which the course belongs.
				if ( (tabuTimeslot_curriculum.get(kdt) != null && Collections.disjoint(tabuTimeslot_curriculum.get(kdt), course_i.belongsToCurricula)) || tabuTimeslot_curriculum.get(kdt) == null) {
					// teacher not banned, and no curricula to which the course belongs are banned during the timeslot, course_i could be assigned
					aps +=1;
					if ( (checkedKDT.contains(kdt) == false) || (checkedKDT == null)) {
						//if timeslot not already checked for apd
						apd+=1;
						checkedKDT.add(kdt);
					}
				}
			}
		});
		int[] apd_aps = {apd,aps};
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

	public static  void initializeTempSolutionToNull() {
		for(int i=0; i < totalDays; i++) {
			for(int k=0; k < timeslotsPerDay; k++) {
				for(int r=0; r<listRooms.size(); r++) {
					KeyDayTimeRoom kdtr_ini = new KeyDayTimeRoom(i,k,listRooms.get(r));
					tempSolution.putIfAbsent(kdtr_ini, null);
				}
			}
		}
	}
	
	public static void replicateCoursesFromListCoursestoListCurricula() {
         	listCurricula.stream().forEach(curr -> {
			curr.coursesThatBelongToCurr.stream().forEach( curso -> {
				Course[] newer = new Course[1];
				newer[0] = listCourses.stream().filter(curs_f -> curs_f.courseID == curso.courseID ).findAny().get();
				curso.belongsToCurricula = newer[0].belongsToCurricula;
				curso.constraintsRoom = newer[0].constraintsRoom;
				curso.constraintsTimeslot = newer[0].constraintsTimeslot;
				curso.courseID = newer[0].courseID;
				curso.doubleLectures = newer[0].doubleLectures;
				curso.minWorkDays = newer[0].minWorkDays ;
				curso.numberOfLectures = newer[0].numberOfLectures;
				curso.numberOfUnassignedLectures = newer[0].numberOfUnassignedLectures;
				curso.students = newer[0].students;
				curso.teacherID = newer[0].teacherID;
						
			});
		});
	}
	

	
	    public static void main_(String[] args) {



		long startTime = System.nanoTime();
		
		
		String pathToXml = "/Users/alexandrubucur/Documents/workspace/TabuSearch/Input_Daten/UniUD_xml/Udine1.xml";
		//String pathToXml = "/Users/bucura/workspace/TabuSearch/Input_Daten/UniUD_xml/Udine1.xml";
		Parser.setxmlFile(pathToXml);
		Parser.getData();
		// make sure the listCourses and listCurricula start with the same definition of courses
		replicateCoursesFromListCoursestoListCurricula();
		
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println((double) estimatedTime / 1000000000.0 + " seconds for importing the dataset from XML");
			initializeTempSolutionToNull();


		//Debug
		//System.out.println(tempSolution.size());
		//tempSolution.keySet().forEach(key -> {if (key.getRoom().getRoomID().equals("r36")) {System.out.println(key.Day +" "+ key.Timeslot +" " + key.getRoom().getRoomID() + " " + key.getRoom().getRoomCapacity() + " " + key.getRoom().getBuilding());}});
		//System.out.println(tempSolution.get(new KeyDayTimeRoom(1, 3, new Room("r36",42,"0"))).toString());
		//System.out.println("in listRooms searched for r36 and found "+listRooms.stream().filter(room -> room.getRoomID().equals("r36")).findAny().get().toString());
		//System.out.println("size of listRooms " + listRooms.size());
		//listRooms.stream().forEach(room -> System.out.println(room.getRoomID() + " "+room.getRoomID().toCharArray().length + " " + room.getRoomCapacity() + " " + room.getBuilding()));
		//System.out.println("listRooms contains the needed room -> "+listRooms.contains(new Room("Er1", 70, "0")));
		//System.out.println(listRooms.stream().filter(room -> room.getRoomCapacity() == 336).count());
		//System.out.println(tempSolution.containsKey(new KeyDayTimeRoom(1, 3, new Room("r36", 42, "0"))));
		//tempSolution.entrySet().stream().filter(entry -> entry.getKey().assignedRoom.getRoomID().equals("r36")).forEach(entry -> System.out.println(entry.getKey().Timeslot));
		//tempSolution.entrySet().stream().filter(entry -> entry.getValue() == null).forEach(entry -> System.out.println("yeah"));
        //int[] ul = new int[1];
        // ul[0] = 0;
        //listCourses.forEach( course -> ul[0]+= course.numberOfUnassignedLectures);
        //System.out.println("unassigned total -> "+ ul[0]);

			long startTimeWholePopulation = System.nanoTime();

		 /**
		  *  Generate the population
		  */

		for (int i=0; i < popSize; i++) {
         tempSolution.clear();
         initializeTempSolutionToNull();
         tabuTimeslot_teacher.clear();
         tabuTimeslot_curriculum.clear();
         course_rooms_map.clear();
         course_day_map.clear();
         
         //reassign the full number of courses to listCourses and list Curricula
         listCourses.stream().forEach(kurs -> {
			 kurs.numberOfUnassignedLectures = kurs.numberOfLectures;
		 });
         
         listCurricula.stream().forEach(curriculum -> {
			 curriculum.coursesThatBelongToCurr.forEach(curso -> {
				 curso.numberOfUnassignedLectures = curso.numberOfLectures;
			 });
		 });

		 //Debug
		  /*  listCourses.stream().forEach(kurs -> {
				System.out.println("unassigned " + kurs.numberOfUnassignedLectures + " total "+ kurs.numberOfLectures);
			}); */


         /**
          * Generation of tempSolution, one entry of the population
          */
         startTime = System.nanoTime();
         while ( listCourses.stream().filter(course -> course.numberOfUnassignedLectures > 0).count() > 0 )
         {

             //Debug
         int[] howManyLeft = new int[1];
         howManyLeft[0] = 0;
         listCourses.stream().filter(course -> course.numberOfUnassignedLectures > 0).forEach(course -> {
             howManyLeft[0] += course.numberOfUnassignedLectures;
         });
             //Debug
         //System.out.println(ANSI_PURPLE + "lectures left -> " + howManyLeft[0] + ANSI_RESET);


         /**
          * insert hier ABBRUCH BEDINGUNG FALLS KEINE WEITERE LECTURES ASSIGNED WERDEN KÖNNEN	 
          */
		 /***************** Choose a course according to HR 1 *****************/
		 /***************** courses with small numbers of available periods and large number of unassigned lectures have priority *****************/
		 
         checkedKDT.clear(); // gehört es wirklich hier?
		 Map<Course,Double> course_apd_i = new HashMap<>();
		 Map<Course,Double> course_aps_i = new HashMap<>();
		 course_apd_i.clear();
		 course_aps_i.clear();
		 double min_apd = 0;
		 double min_aps = 0;
		 double max = 0;
		 Course selectedCourse = null;
		 // fill the maps with the courses and their corresponding apd and aps divided by the number of unassigned lectures
		 listCourses.stream().filter(course -> course.numberOfUnassignedLectures > 0).forEach((course) -> {
		     int[] apd_aps = new int[2];
	         apd_aps = calc_apd_aps(course,tempSolution);
		     double unassignedLectures = course.numberOfUnassignedLectures;
		     course_apd_i.putIfAbsent(course, apd_aps[0]/Math.sqrt(unassignedLectures));
		     course_aps_i.putIfAbsent(course, apd_aps[1]/Math.sqrt(unassignedLectures));
		 });		 
		 
		 // DEBUG
		// System.out.println("Size of course_apd_i map is " + course_apd_i.size() + " Size of course_aps_i map is "+course_aps_i.size() );
		 //System.out.println(listCourses.get(23).courseID);
		 // System.out.println("constraints of course c0163 containts room DS1 " + listCourses.get(23).constraintsRoom.contains(new Room("Er2",70,"0")));
		//listCourses.get(15).constraintsRoom.stream().forEach(entry -> System.out.println(entry.getRoomID()));
			//System.out.println(listCourses.stream().filter( entry -> entry.constraintsRoom.contains(new Room("DS1",(Integer) 60,"0"))).count());
           // System.out.println(listCourses.get(15).constraintsTimeslot.contains(new KeyDayTime(1,2)));


             /**
			  *  choose  the course with the smallest value of apd_i(X)/sqrt(nl_i(X))
			  */



		//Debug
	 	/*System.out.println("Values in apd");
		course_apd_i.entrySet().stream().forEach(entry -> System.out.print(ANSI_RED + new DecimalFormat("#.##").format(entry.getValue()) + " " + ANSI_RESET));
             System.out.println("\n");
		System.out.println("Values in aps");
		course_aps_i.entrySet().stream().forEach(entry -> System.out.print(ANSI_GREEN + new DecimalFormat("#.##").format(entry.getValue()) + " "+ ANSI_RESET));
		System.out.println("\n");*/

			 // find minimum for apd first
		 min_apd = Collections.min(course_apd_i.values());
		 

		// add all elements that have a value equal to min_apd
		 List<Course> tieCourses_apd = new ArrayList<>(200);
		 List<Course> tieCourses_aps = new ArrayList<>(200);
		 
		 for(Entry<Course, Double> entry : course_apd_i.entrySet()) {
			 if (entry.getValue() - min_apd <= 0.00000000001 && entry.getValue() - min_apd >= -0.00000000001  ) {
				 tieCourses_apd.add(entry.getKey());
			 }
		 }
		 
		//DEBUG
		/* if (tieCourses_apd.size() == 0 ) {
		 System.out.println("Achtung size of tieCourses_apd is zero, but min_apd is "+ min_apd + " and course_apd_i still has "+ course_apd_i.size()+ " elements");
		 System.out.println("tieCourses_apd(0) is "+tieCourses_apd.get(0));
		 }
		 else {
			 System.out.println("Size of tieCourses_apd is "+tieCourses_apd.size());
		 }*/
		 
		 
		 
		 
		// if tie between courses, choose the course with smallest value of aps_i(X)/sqrt(nl_i(X))
		 if (tieCourses_apd.size() > 1 ) {
			// find minimum for aps first
			 min_aps = 1000;
			 min_aps = Collections.min(course_aps_i.values());
			 
			// add all elements that have a value equal to min_aps
			 for(Entry<Course, Double> entry : course_aps_i.entrySet()) {
				 if (entry.getValue() - min_aps <= 0.00000000001  && entry.getValue() - min_aps >= -0.00000000001) {
					 tieCourses_aps.add(entry.getKey());
				 }
			 }
			 
			 //DEBUG
			/* if (tieCourses_aps.size() == 0) {
			 System.out.println("Achtung size of tieCourses_aps is zero, but min_apd is "+ min_aps);
			 }
			 else {
				 System.out.println("Size of tieCourses_aps is "+tieCourses_aps.size());
			 } */
			 
			 
			 if (tieCourses_aps.size()>1) {
				 // still courses with ties even for aps, now decide based on number of courses that share common students or teacher with course c_i
				 // that means, the course with max : curricula to which it belongs x courses in curricula
				 Map<Course,Integer> con_f_i  = new HashMap<>(tieCourses_aps.size());
                 con_f_i.clear();

				 tieCourses_aps.stream().forEach((course) -> {
					 
					 con_f_i.putIfAbsent(course, 0);
                     int[] sumable = new int[1];
                     sumable[0] = 0;

					 currForCourse(course).forEach((curr) -> {
						 //courses that share common students -> courses in curricula in which the course is also present
                         sumable[0] += curr.coursesThatBelongToCurr.size();
					 });
					 //courses that share a common teacher
					   sumable[0] +=  listCourses.stream().filter( kurs -> kurs.teacherID == course.teacherID).count();

                     con_f_i.put(course,sumable[0]);
				 });
				 
					// find maximum for con_f_i first
				    max = (double) Collections.max(con_f_i.values());

                 //con_f_i.entrySet().stream().forEach(entry -> System.out.print(entry.getValue() + " "));
                 //System.out.println("\n***************** -> " + Collections.max(con_f_i.values()));

                List<Course> tie_after_con_f_i = new ArrayList<>(con_f_i.size());

			   	for(Entry<Course, Integer> entry_ : con_f_i.entrySet()) {
					     if (entry_.getValue() - max <= 0.00000000001 && entry_.getValue() - max >= -0.00000000001 ) {
                             //selectedCourse = entry_.getKey();
                             tie_after_con_f_i.add(entry_.getKey());
                         }
					 }
                 if (tie_after_con_f_i.size() > 0) {
                     SecureRandom rangen = new SecureRandom();
                     selectedCourse = tie_after_con_f_i.get(rangen.nextInt(tie_after_con_f_i.size()));
                 }
                 else {
                     System.out.println("Something really weird going on, no course could be assigned even after con_f_i!");
                     selectedCourse = null;
                 }
				  //DEBUG
					// System.out.println("Achtung NO max con_f_i, size of con_f_i is " + con_f_i.size() + " max is "+ max + " selectedCourse con_f_i ist "+con_f_i.get(selectedCourse));
				 }
			 else{
				 //only one element with smallest aps_i, select course
                 //System.out.println("Course selected with aps");
				 selectedCourse = tieCourses_aps.get(0);
			 }
		 }
		 else {
			 // only one element with smallest apd_i, select course
             //Debug
             //System.out.println("Course selected directly with apd");

			 selectedCourse = tieCourses_apd.get(0);
		 }
		 
		 
		 /***************** Assign the course according to HR 2 *****************/
		 /***************** select a period among all available ones that is least likely to be used by other unfinished courses at later steps *****************/

         timeslotRoom_g_jk_map.clear();
		 Course[] chosenCourse = new Course[1];
		 chosenCourse[0] = selectedCourse;
			 //Debug
		 //System.out.print(ANSI_CYAN + selectedCourse.courseID+ " ");
		 
		 // for each available period-room pair choose the pair with the smallest value of g(j,k) = k_1 * uac_i_j(X) + k_2 * Delta_f_s(i,j,k)


		// List<Map.Entry<KeyDayTimeRoom, Course>> course_still_null = tempSolution.entrySet().stream().filter( kdtr -> kdtr.getValue() == null).collect(Collectors.toList());

         //Debug
            // System.out.println(ANSI_GREEN + course_still_null.size() + ANSI_RESET);

		 //Debug
         /* for(Entry e:course_still_null) {
             if (e.getValue() != null) {
                 System.out.println("sht!!");
             }
         }*/
/*
		 if(chosenCourse[0] == null) {
		 System.out.println("*********** HR1 CHOSEN COURSE IS NULL " + chosenCourse[0] == null );
		 }
         List<Map.Entry<KeyDayTimeRoom, Course>> course_still_null_ = null;
		 if (chosenCourse[0].constraintsRoom != null ) {
           course_still_null_=  course_still_null.stream().filter(entry -> (chosenCourse[0].constraintsRoom.contains(entry.getKey().assignedRoom) == false)).collect(Collectors.toList());
		 }
             List<Map.Entry<KeyDayTimeRoom, Course>> course_still_null__ = null;
		 if (chosenCourse[0].constraintsTimeslot != null ) {
             course_still_null__ = course_still_null_.stream().filter(entry -> chosenCourse[0].constraintsTimeslot.contains(new KeyDayTime(entry.getKey().Day,entry.getKey().Timeslot)) == false).collect(Collectors.toList());
		 }   */

          Map<KeyDayTimeRoom, Course> go_through_these = new HashMap<>();
             go_through_these.clear();
         for (Map.Entry<KeyDayTimeRoom,Course> nullo : tempSolution.entrySet()) {
             if (nullo.getValue() == null) {
                 if (chosenCourse[0].constraintsRoom.contains(nullo.getKey().assignedRoom) == false) {
                     if (chosenCourse[0].constraintsTimeslot.contains(new KeyDayTime(nullo.getKey().Day,nullo.getKey().Timeslot)) == false) {
                         go_through_these.put(nullo.getKey(), nullo.getValue());
                        // System.out.println(ANSI_GREEN + tempSolution.containsKey(nullo.getKey()) + ANSI_RESET);
                     }
                 }
             }
         }


         //Debug
         //System.out.println("Still free places for the course "+ chosenCourse[0].courseID +" -> " + course_still_null.size());
         /* if(chosenCourse[0].courseID == 1074) {
             System.out.println("Count of courses with unassigned lectures : "+listCourses.stream().filter(course -> course.numberOfUnassignedLectures > 0).count());
         }*/


            if (go_through_these == null || go_through_these.size() <= 0) {
                System.out.println("Houston we have a problem...");
            }


			go_through_these.entrySet().stream().forEach(entry -> {
                //check for feasibility of entry, where teacher or curriculum banned
                int Day = entry.getKey().getDay();
                int Timeslot = entry.getKey().getTimeslot();
                KeyDayTime kdt = new KeyDayTime(Day, Timeslot);
                if (tabuTimeslot_teacher.get(kdt) == null || tabuTimeslot_teacher.get(kdt).contains(chosenCourse[0].teacherID) == false) {
                    //teacher has not been banned yet, check if curricula bans for timeslot contain any curriculum to which the course belongs.
                    if (tabuTimeslot_curriculum.get(kdt) == null || Collections.disjoint(tabuTimeslot_curriculum.get(kdt), chosenCourse[0].belongsToCurricula)) {
                        //feasible insertion, check value of g(j,k) and add it to map (don't assign the course, just add to map kdtr -> g(j,k))

                        int[] uac_ij = new int[1];
                        uac_ij[0] = 0;
                        //count courses with same teacher
                        uac_ij[0] = (int) listCourses.stream().filter(curso -> curso.teacherID == chosenCourse[0].teacherID).count();
                        // count courses from same curricula * number of unfinished lectures of that course
                        chosenCourse[0].belongsToCurricula.forEach(curriculum -> {
                            curriculum.coursesThatBelongToCurr.forEach(course_in_curr -> {
                                uac_ij[0] += course_in_curr.numberOfUnassignedLectures;
                            });
                        });

                        //calculate soft penalties
                        int[] soft_penalty = new int[1];
                        soft_penalty[0] = 0;

                        //S1 enrolled students - room capacity  -> konstante a_1 = 1
                        int enrolledStudents = chosenCourse[0].students;

                        if (enrolledStudents > entry.getKey().getRoom().getRoomCapacity()) {
                            soft_penalty[0] += 1 * (enrolledStudents - entry.getKey().getRoom().getRoomCapacity());
                        }

                        //S2 room stability -> konstante a_2 = 1
                        soft_penalty[0] += (course_rooms_map.entrySet().stream().count() - 1);

                        //S3 minimum working days -> konstante a_3 = 5
                        if (course_day_map.get(chosenCourse[0]) == null || course_day_map.get(chosenCourse[0]).size() < chosenCourse[0].minWorkDays) {

                            int days_in_map = 0;
                            if (course_day_map.get(chosenCourse[0]) == null) {
                                days_in_map = 0;
                            } else {
                                days_in_map = course_day_map.get(chosenCourse[0]).size();
								if (course_day_map.get(chosenCourse[0]).contains(entry.getKey().getDay()) == false) {
									days_in_map +=1;
								}
                            }
                            soft_penalty[0] += 5 * (chosenCourse[0].minWorkDays - days_in_map);
                        }

                        //S4 curriculum compactness -> konstante a_4 = 2

                        //check only previous timeslot, all other ones are the same
                        Map<Integer, List<Curriculum>> timeSlot_Curricula = new HashMap<>(2);

                        if (Timeslot > 1) {
                            List<Map.Entry<KeyDayTimeRoom, Course>> reduced_before = tempSolution.entrySet().stream().filter(eintrag -> eintrag.getKey().Day == Day && eintrag.getKey().Timeslot == (Timeslot - 1)).collect(Collectors.toList());
                            // tempSolution.entrySet().stream().filter(eintrag -> eintrag.getKey().Day == Day && eintrag.getKey().Timeslot == Timeslot - 1).forEach( timeRoom -> {
                            for (int z = 0; z < reduced_before.size(); z++) {
                                List<Curriculum> listToAdd = new ArrayList<>(listCurricula.size()*2);
                                if (timeSlot_Curricula.get(Timeslot - 1) != null) {
                                    listToAdd = timeSlot_Curricula.get(Timeslot - 1);
                                }
                                if (reduced_before.get(z).getValue() != null) {
                                    //System.out.println("the size of the curriculalist in the previous timeslot "+ reduced_before.get(z).getValue().belongsToCurricula.size());
                                    List<Curriculum> myList = reduced_before.get(z).getValue().belongsToCurricula;
                                    listToAdd.addAll(myList);
                                }
                                timeSlot_Curricula.put(Timeslot - 1, listToAdd);
                            }
                            //  });
                            tempSolution.entrySet().stream().filter(eintrag -> eintrag.getKey().Day == Day && eintrag.getKey().Timeslot == Timeslot).forEach(timeRoom -> {
                                List<Curriculum> listToAdd_ = new ArrayList<>();
                                if (timeSlot_Curricula.get(Timeslot) != null) {
                                    listToAdd_ = timeSlot_Curricula.get(Timeslot);
                                }
                                if (timeRoom.getValue() != null) {
									List<Curriculum> myOtherList = timeRoom.getValue().belongsToCurricula;
                                    listToAdd_.addAll(myOtherList);
                                }
                                List<Curriculum> currChosen = chosenCourse[0].belongsToCurricula; //ist in Ordnung
                                listToAdd_.addAll(currChosen);
                                timeSlot_Curricula.put(Timeslot, listToAdd_);
                            });

                            //every curriculum in the new timeslot which is not present in the previous one is one violation
                            //timeSlot_Curricula.get(new KeyDayTime(Day,Timeslot)).stream().forEach(curr -> {
                            timeSlot_Curricula.get(Timeslot).stream().forEach(curr -> {
                                //	if (timeSlot_Curricula.get(new KeyDayTime(Day,Timeslot-1)).contains(curr) == false) {
                                if (timeSlot_Curricula.get(Timeslot - 1).contains(curr) == false) {
                                    soft_penalty[0] += 2;
                                }
                            });
                        } // if timeslot > 1

                        //calculate g_j_k
                        timeslotRoom_g_jk_map.put(entry.getKey(), uac_ij[0] + 0.5 * soft_penalty[0]);
                    }//if curricula not banned
                }
            });//calculation of g_jk from tempSolution
		 
		 //select the entry with the minimum value of g_jk
		 Double[] min_g_jk = new Double[1];
		 min_g_jk[0] = Double.MAX_VALUE;


          KeyDayTimeRoom chosen_kdtr = null;
          Boolean performedInsertion = false;

			 if (timeslotRoom_g_jk_map.size() > 0) {
			 min_g_jk[0] = Collections.min(timeslotRoom_g_jk_map.values());

			 List<Map.Entry<KeyDayTimeRoom, Double>> zwischenSpeicher = timeslotRoom_g_jk_map.entrySet().stream().filter(entry__ -> (entry__.getValue() - min_g_jk[0] < 0.0000001) && (entry__.getValue() - min_g_jk[0] > -0.0000001)).collect(Collectors.toList());

				 if (zwischenSpeicher != null && zwischenSpeicher.size() > 0 ) {
					 SecureRandom rangen = new SecureRandom();
					 chosen_kdtr = zwischenSpeicher.get(rangen.nextInt(zwischenSpeicher.size())).getKey();
				 }
             //Debug
              //System.out.println("chosen_kdtr : " + chosen_kdtr.toString());
             //implement the feasible lecture insertion

                 //System.out.println(tempSolution.containsKey(chosen_kdtr));

				 if(chosen_kdtr != null && (tempSolution.get(chosen_kdtr) == null)) {
                     try {
                         tempSolution.put(chosen_kdtr, chosenCourse[0]);
                     }
                     catch (Exception ex) {System.out.println("\nHoly fuck!!!\n");}
                     performedInsertion = true;

				 }
				 else {
                     if (chosen_kdtr == null) {System.out.println("chosen_kdtr is null");}
                     else if (tempSolution.get(chosen_kdtr) != null) {
                         System.out.println("Want to insert c"+chosenCourse[0].courseID+ " but the kdtr is occupied by " + tempSolution.get(chosen_kdtr).courseID );
                     }
                     performedInsertion = false;
				 }
         }
         else {
             //no insertion
             System.out.println(ANSI_RED + "The timeslotRoom_g_jk_map size is zero and no insertion can be made!" + ANSI_RESET);
         }
		 //assign the kdtr with smallest value of g_jk



            //update all the necessary lists!
		 //update the map which keeps track of the days on which the course takes place
           if (chosen_kdtr != null && chosenCourse[0] != null && performedInsertion == true ) {
               List<Integer> list_days = new ArrayList<>();

               if (course_day_map.get(chosenCourse[0]) != null) {
                   list_days = course_day_map.get(chosenCourse[0]);
               }
               list_days.add(chosen_kdtr.Day);
               list_days = Funktionen.removeDuplicatesInteger(list_days);
               course_day_map.put(chosenCourse[0], list_days);

               //update the map which keeps track of the days on which the course takes place
               List<Room> list_rooms = new ArrayList<>();
               if (course_rooms_map.get(chosenCourse[0]) != null) {
                   list_rooms = course_rooms_map.get(chosenCourse[0]);
               }
               list_rooms.add(chosen_kdtr.getRoom());
               list_rooms = Funktionen.removeDuplicatesRoom(list_rooms);
               course_rooms_map.put(chosenCourse[0], list_rooms);


               //Debug
               //System.out.println(" FIRST : Number of unassigned lectures of course "+ chosenCourse[0].courseID +" : "+ chosenCourse[0].numberOfUnassignedLectures);

               listCourses.get(listCourses.indexOf(chosenCourse[0])).numberOfUnassignedLectures -= 1;

               //Debug
               // System.out.println(" Info : the Course takes place in " + chosenCourse[0].belongsToCurricula.size() + " curricula");
               // System.out.println(" SECOND : Number of unassigned lectures of course "+ chosenCourse[0].courseID +" : "+ chosenCourse[0].numberOfUnassignedLectures);



               //tabuTimeslot_teacher
               KeyDayTime lastKDT = new KeyDayTime(chosen_kdtr.getDay(), chosen_kdtr.getTimeslot());
               if (tabuTimeslot_teacher.get(lastKDT) != null) {
                   tabuTimeslot_teacher.get(lastKDT).add(chosenCourse[0].teacherID);
               } else {
                   List<Integer> bannedTeachies = new ArrayList<>(1);
                   bannedTeachies.add(chosenCourse[0].teacherID);
                   tabuTimeslot_teacher.put(lastKDT, bannedTeachies);
               }
		/* List<Integer> banned_teachers = tabuTimeslot_teacher.get(lastKDT);
		 tabuTimeslot_teacher.put(lastKDT, banned_teachers);*/

               //tabuTimeslot_curriculum
               if (tabuTimeslot_curriculum.get(lastKDT) != null) {
                   tabuTimeslot_curriculum.get(lastKDT).addAll(chosenCourse[0].belongsToCurricula);
               } else {
                   List<Curriculum> banned_curricula = new ArrayList<>();
                   banned_curricula.addAll(chosenCourse[0].belongsToCurricula);
                   tabuTimeslot_curriculum.put(lastKDT, banned_curricula);
               }
		/* List<Curriculum> banned_curricula = tabuTimeslot_curriculum.get(lastKDT);
		 tabuTimeslot_curriculum.put(lastKDT, banned_curricula); */
           }
             timeslotRoom_g_jk_map.clear();


         } // while unfinished lectures still there
		 //after no more unfinished lectures to assign or Abbruch Bedingung, assign tempSolution to population
         population.add(tempSolution);

        //Debug

        tempSolution.entrySet().stream().forEach(entry -> {
            if (entry.getValue() != null && entry.getValue().courseID == 980) {
                //System.out.println("Allelujah! " + entry.getKey().getDay() + " " + entry.getKey().getTimeslot() + " " + entry.getKey().getRoom().getRoomID());
            }
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });



 	    estimatedTime = System.nanoTime() - startTime;
 		System.out.println(i +"   "+ (double) estimatedTime / 1000000000.0 + " seconds for one member of population");

		System.out.print("\n");
		} // for i<populationSize




		// Debug
		//System.out.println("Same collection is the same : "+ Funktionen.MapisEqual(population.get(1),population.get(1)));

		for(int g=1;g<population.size(); g++) {
			Map<KeyDayTimeRoom,Course> mappy_1 = population.get(g);
			Map<KeyDayTimeRoom,Course> mappy_2 = population.get(g-1);
			if (  Funktionen.MapisEqual(mappy_1, mappy_2)) {
				System.out.println("popGen : Solution "+g+" equals "+ (g-1));
			}
			else {
				System.out.println("popGen:The solutions are not all the same!!!");
			}
		}

		long estimatedTimeWholePopulation = System.nanoTime() - startTimeWholePopulation;
		System.out.println((double) estimatedTimeWholePopulation / 1000000000.0 + " seconds for the whole population of " +popSize);


		//Copy population to the population of the algorithm class in the memeticAlgo package
		memeticAlgo.Algorithm.population = population;
		memeticAlgo.Algorithm.numDays = totalDays;
		memeticAlgo.Algorithm.timeslotsPerDay = timeslotsPerDay;
		memeticAlgo.Algorithm.popSize = popSize;
		memeticAlgo.Algorithm.numCourses = numCourses;
		memeticAlgo.Algorithm.numRooms = listRooms.size();
		memeticAlgo.Algorithm.numCurricula = listCurricula.size();
		memeticAlgo.Algorithm.roomList = listRooms;
		memeticAlgo.Algorithm.CurriculumList = listCurricula;


		// Debug - See Timetable
		population.stream().forEach(map -> {
            listRooms.stream().forEach(roomie -> {
                String RoomieID = roomie.getRoomID();
                while (RoomieID.chars().count() < 5) {
                    RoomieID += " ";
                }
                System.out.print("\n\nRoom " + RoomieID + "    ");
                for (int r = 0; r < totalDays; r++) {
                    for (int e = 0; e < timeslotsPerDay; e++) {
                        String cursoID = "Free";
                        Course curso = map.get(new KeyDayTimeRoom(r, e, roomie));
                        if (curso != null) {
                            cursoID = curso.courseID.toString();
                            while (cursoID.chars().count() < 4) {
                                cursoID += " ";
                            }
                        }
                        System.out.print(cursoID + "    ");
                    }
                }
            }); //roomie

            System.out.print("\n\n\n\n\n\n\n");
        }); //population for each stream


		//DEBUG - see all curricula in a day
		/*tempSolution.entrySet().stream().filter(entry -> entry.getKey().getDay() == 2 && entry.getKey().getTimeslot() == 2).forEach(entry -> {
			if (entry.getValue() != null) {
			entry.getValue().belongsToCurricula.stream().forEach(curr -> System.out.print( curr.curriculumID + "  " ));
			System.out.println("\n\n");
			}
		}); */
		
		
	}//main
}
