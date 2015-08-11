package parser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import objects.*;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import comparator.RoomDemandComparator;

import javax.xml.parsers.ParserConfigurationException; 


public class Parser {

	private String xmlFile;
	private File fXmlFile;
	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document doc;
	private Element e;
	private NodeList nList;


	private int numberofcourses, numberofrooms, numberofperiods, numberofdays, numberofcurricula, numberofcoursespC, numberofS;
	private ArrayList<Timeslot> timeslots;
	private ArrayList<String> courseIDs;
	private HashMap<String, CourseFeatures> courses;
	private ArrayList<Curriculum> curricula; // HashMap ??
	private ArrayList<String> roomIDs;
	private HashMap<String, RoomFeatures> rooms;
	private ArrayList<String> teacherIDs = new ArrayList<String>();
	private ArrayList<Vertex> Vconf;
	private Set<Edge> Econfset = new HashSet<Edge>();
	private ArrayList<Edge> Econf = new ArrayList<Edge>();
	private ArrayList<ArrayList<String>> definingCsets = new ArrayList<ArrayList<String>>();
	private HashMap<Vertex,Integer> prio;
	private HashMap<String, ArrayList<Timeslot>> eligibleTimeslots; //T(c)
	private HashMap<String, ArrayList<String>> eligibleRooms; //R(c)
	private HashMap<String, ArrayList<String>> eligibleCoursesperTeacher; //T(c)
	private ArrayList<Vertex> eligibleRoomTimeslotcombinations;

	private ArrayList<Integer> periodsAroundNoon;
	private int numberofTfLunch;
	private ArrayList<Integer> pafterPreferedEndTime;
	private ArrayList<Integer> pbeforePreferedStartTime;
	private ArrayList<Integer> pWednesdayAfternoon;

	private ArrayList<Integer> lS;
	private ArrayList<Integer> Rs; //for each room capacity, the number of rooms with a greater capacity are stored
	private ArrayList<ArrayList<String>> Cs; //for each room capacity si, the courses with a greater demand than si but a smaller demand than si+1 

	public void setxmlFile(String xmlFile){
		try {
			//this.xmlFile = xmlFile;
			Path xmlPath = FileSystems.getDefault().getPath("/Users/alexandrubucur/Documents/workspace/TabuSearch/Input_Daten/UniUD_xml/Udine1.xml");
			fXmlFile = new File(xmlFile); 
			dbFactory = DocumentBuilderFactory.newInstance(); 
			dBuilder = dbFactory.newDocumentBuilder(); 
			doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	//loads the whole data from a xml-file
	public Data getData(){ 

		getAvailableTimeslots();
		getCourses();
		getRooms();
		getCurricula();
		geteligibleCombinations();
		getEconf();


		periodsAroundNoon = new ArrayList<Integer>();
		periodsAroundNoon.add(2);
		numberofTfLunch = 1;

		pafterPreferedEndTime = new ArrayList<Integer>();
		pafterPreferedEndTime.add(4);
		pbeforePreferedStartTime = new ArrayList<Integer>();
		pbeforePreferedStartTime.add(0);
		pWednesdayAfternoon = new ArrayList<Integer>();
		pWednesdayAfternoon.add(3);
		pWednesdayAfternoon.add(4);

		getPrio(2,2,2);

		//TODO: get definingCsets


		//get power set of C
		//definingCsets = getPowSet(courseIDs);	

		eligibleRoomTimeslotcombinations = geteligibleRoomTimeslotsCombinations();

		getRsandCs();

		return new Data(numberofperiods, numberofdays, timeslots, courseIDs, courses, curricula, roomIDs, rooms, teacherIDs, Vconf, Econf, definingCsets, prio, eligibleTimeslots, eligibleRooms, eligibleCoursesperTeacher, eligibleRoomTimeslotcombinations,periodsAroundNoon,numberofTfLunch,lS,Rs,Cs);
	}

	public SatData getSatData(){

		getAvailableTimeslots();
		getCourses();
		getRooms();
		getCurricula();

		return new SatData(numberofperiods, numberofdays, timeslots, courseIDs, courses, curricula, roomIDs, rooms, teacherIDs);

	}

	private void getAvailableTimeslots() {
		nList = doc.getElementsByTagName("descriptor");
		e = (Element) nList.item(0);

		nList = doc.getElementsByTagName("periods_per_day");
		e = (Element) nList.item(0);
		numberofperiods = Integer.parseInt(e.getAttribute("value"));

		nList = doc.getElementsByTagName("days");
		e = (Element) nList.item(0);
		numberofdays = Integer.parseInt(e.getAttribute("value"));

		timeslots = Timeslot.getTimeslots(numberofperiods, numberofdays);

	}

	private void getCourses() {
		nList = doc.getElementsByTagName("courses");
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("course");
		numberofcourses = nList.getLength();

		courseIDs = new ArrayList<String>(numberofcourses);
		courses = new HashMap<String,CourseFeatures>(numberofcourses*2);

		eligibleCoursesperTeacher = new HashMap<String,ArrayList<String>>();

		ArrayList<String> coursesperTeacher;
		boolean doublelectures;
		int numberStudents;
		String teacher;
		String ID;
		int min_days;
		int lectures;

		for(int i = 0; i < numberofcourses;i++){
			e = (Element) nList.item(i);

			if(e.getAttribute("double_lectures") == "yes")
				doublelectures = true;
			else	
				doublelectures = false;

			numberStudents = Integer.parseInt(e.getAttribute("students"));
			ID = e.getAttribute("id");


			teacher = e.getAttribute("teacher");

			if(!teacherIDs.contains(teacher)){  //TODO: change, if better structure of coursesperTeacher is available
				coursesperTeacher = new ArrayList<String>();
				coursesperTeacher.add(ID);
				teacherIDs.add(teacher);
				eligibleCoursesperTeacher.put(teacher,coursesperTeacher);
			}
			else{
				coursesperTeacher = eligibleCoursesperTeacher.get(teacher);
				coursesperTeacher.add(ID);
				eligibleCoursesperTeacher.put(teacher,coursesperTeacher);
			}


			min_days = Integer.parseInt(e.getAttribute("min_days"));
			lectures = Integer.parseInt(e.getAttribute("lectures"));

			courseIDs.add(ID);
			courses.put(ID,new CourseFeatures(doublelectures,numberStudents,teacher,min_days,lectures,ID));


		}

	}


	private void getRooms(){
		nList = doc.getElementsByTagName("rooms");
		String ID;
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("room");
		int numberofrooms = nList.getLength();
		lS = new ArrayList<Integer>();

		roomIDs = new ArrayList<String>(numberofrooms);
		rooms = new HashMap<String,RoomFeatures>(numberofrooms*2);
		String building;
		int size;

		for(int i = 0; i < numberofrooms;i++){
			e = (Element) nList.item(i);

			building = e.getAttribute("building");
			ID = e.getAttribute("id");
			size = Integer.parseInt(e.getAttribute("size"));
			lS.add(size); //add room capacity

			roomIDs.add(ID);
			rooms.put(ID,new RoomFeatures(building,size,ID));

		}

	}

	private void getRsandCs(){ 


		Collections.sort(lS); //sorted list with all room capacities (also double values)	
		int s = lS.get(0);
		Rs = new ArrayList<Integer>();
		Cs = new ArrayList<ArrayList<String>>();
		Rs.add(1);
		Cs.add(new ArrayList<String>());

		//generate the number of rooms for each size s and deletes double values in lS
		for(int i = 1; i < lS.size();i++){

			if(s == lS.get(i)){
				Rs.set(i-1, Rs.get(i-1)+1);
				lS.remove(i);
				i--;
			}
			else{
				s = lS.get(i);
				Rs.add(1);
				Cs.add(new ArrayList<String>());
			}
		}

		numberofS = lS.size();

		//Generate |R>s| ... number of rooms with capacity greater s
		for(int i = numberofS-2; i>0;i--){
			Rs.set(i,Rs.get(i)+Rs.get(i+1));
		}

		Rs.remove(0); 

		ArrayList<String> sortedCourseIDs = new ArrayList<String>();
		sortedCourseIDs.addAll(courseIDs);

		RoomDemandComparator rc = new RoomDemandComparator(courses);
		Collections.sort(sortedCourseIDs,rc); //sorts courseIDs after their demand of seats


		s = lS.get(0);
		int i = 1;

		//Delete all courses with demand equal to min capacity of seat
		while(courses.get(sortedCourseIDs.get(0)).getNumberStudents() == s){
			sortedCourseIDs.remove(0);
		}

		s = lS.get(i);

		for (String cID:sortedCourseIDs){

			while(!(courses.get(cID).getNumberStudents() <= s)){
				i++;
				if(i < numberofS)
					s = lS.get(i);
				else
					s = Integer.MAX_VALUE;
			}

			Cs.get(i-1).add(cID);
		}
	}

	
	
	private void getCurricula(){
		nList = doc.getElementsByTagName("curricula");
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("curriculum");
		numberofcurricula = nList.getLength();


		NodeList nCourses;

		curricula = new ArrayList<Curriculum>(numberofcurricula);
		String id;
		ArrayList<String> courseIDspC = null;

		for(int i = 0; i < numberofcurricula;i++){
			e = (Element) nList.item(i);
			id = e.getAttribute("id");

			//get all courseIds, which refer to the curriculum
			nCourses = e.getElementsByTagName("course");
			numberofcoursespC = nCourses.getLength();
			courseIDspC = new ArrayList<String>(numberofcoursespC);


			for(int j= 0; j < numberofcoursespC;j++){ 
				e = (Element) nCourses.item(j);
				courseIDspC.add(e.getAttribute("ref"));
			}

			curricula.add(new Curriculum(id,courseIDspC)); 

		}
	}

	private void geteligibleCombinations() {
		//get eligible timeslots and rooms for each course 
		//get vertex--> A vertex (c,t) represents an eligible combination of course an timeslot
		eligibleTimeslots = new HashMap<String, ArrayList<Timeslot>>(numberofcourses*2);
		eligibleRooms = new HashMap<String, ArrayList<String>>(numberofcourses*2);
		Vconf = new ArrayList<Vertex>();
		String id;

		ArrayList<Timeslot> at;
		ArrayList<String> ar;

		nList = doc.getElementsByTagName("constraints");
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("constraint");
		int numberofconstraints = nList.getLength();
		int numberoftimeslots;

		NodeList nList2;
		Timeslot ts;


		for(int i = 0; i < numberofconstraints;i++){
			e = (Element) nList.item(i);
			id = e.getAttribute("course");
			at = new ArrayList<Timeslot>();
			ar = new ArrayList<String>();


			//get eligible rooms
			if(e.getAttribute("type").equals("room")){
				nList2 = e.getElementsByTagName("room");
				numberofrooms = nList2.getLength();

				for(int j= 0; j < numberofrooms;j++){
					e = (Element) nList2.item(j);
					ar.add(e.getAttribute("ref"));
				}

				eligibleRooms.put(id, ar);

			}//get eligible timeslots
			else if(e.getAttribute("type").equals("period")){
				nList2 = e.getElementsByTagName("timeslot");
				numberoftimeslots = nList2.getLength();

				for(int j= 0; j < numberoftimeslots;j++){
					e = (Element) nList2.item(j);
					ts = new Timeslot(Integer.parseInt(e.getAttribute("period")), Integer.parseInt(e.getAttribute("day")));
					at.add(ts);
					Vconf.add(new Vertex(id,ts));
				}
				eligibleTimeslots.put(id, at);
			}

		}

	}

	private void getEconf(){
		//if there is no constraint for the specified course, no timeslots are eligible ??? //TODO: How to manage with unsatisfied data?
		/*for(String cID:courseIDs){
			if(!eligibleTimeslots.containsKey(cID)){ 
				eligibleTimeslots.put(cID, new ArrayList<Timeslot>());
			}
		}*/


		//generate Econf, Two nodes (c1,p1) and (c2,p2) are adjacent if p1 = p2 and c1 and c2 refer to the same curriculum
		String c1,c2;
		Vertex v1,v2;

		ArrayList<String> courseIDspC;


		for(Curriculum curriculum: curricula){
			numberofcoursespC = curriculum.getCourses().size();
			courseIDspC = new ArrayList<String>();
			courseIDspC.addAll(curriculum.getCourses());

			for(int i = 0; i < numberofcoursespC-1;i++){
				c1 = courseIDspC.get(i);

				for(Timeslot t: eligibleTimeslots.get(c1)){
					v1 = new Vertex(c1,t);

					for(int j = i+1; j < numberofcoursespC;j++){
						c2 = courseIDspC.get(j);

						if(eligibleTimeslots.get(c2).contains(t))
						{
							v2 = new Vertex(c2,t);
							Econfset.add(new Edge(v1,v2));
						}
					}
				}
			}
		}

		Econf.addAll(Econfset);
	}

	private void getPrio(int Wednesdayafternoon, int pendTime, int pstartTime){
		//get prio(c,t) --> teachers and students preference for course/timeslot combinations (the smaller the number, the higher the priority)

		prio = new HashMap<Vertex,Integer>(Vconf.size()*2);

		for(Vertex v:Vconf){
			prio.put(v, 1);
		}

		Vertex v;
		//Wednesday afternoon free
		for(String cID: courseIDs)
			for(Integer p:pWednesdayAfternoon){
				v = new Vertex(cID,new Timeslot(p,2));
				if(prio.get(v) != null)
					prio.put(v, prio.get(v)+Wednesdayafternoon);
			}

		//Preferred end-time (Reduce number of classes finishing after a specified period)
		for(String cID: courseIDs)
			for(int i=0; i < numberofdays;i++)
				for(Integer p:pafterPreferedEndTime){
					v = new Vertex(cID,new Timeslot(p,i));
					if(prio.get(v) != null)
						prio.put(v, prio.get(v)+pendTime);
				}

		//Preferred start time (Reduce number of classes starting before a specified period)
		for(String cID: courseIDs)
			for(int i=0; i < numberofdays;i++)
				for(Integer p:pbeforePreferedStartTime){
					v = new Vertex(cID,new Timeslot(p,i));
					if(prio.get(v) != null)
						prio.put(v, prio.get(v)+pstartTime);
				}

	}

	private void getdefiningCsets(){
		//TODO 
	}

	private ArrayList<Vertex> geteligibleRoomTimeslotsCombinations(){

		ArrayList<Vertex> rt = new ArrayList<Vertex>();

		for(String rID: roomIDs){
			for(Timeslot t: timeslots){
				rt.add(new Vertex(rID,t));
			}
		}
		
		
		return rt;	
	}


	public static ArrayList<ArrayList<String>> getPowSet(ArrayList<String> A){

		int Asize = A.size();
		int psSize = (int)Math.pow(2, Asize);
		ArrayList<ArrayList<String>> ps = new ArrayList<ArrayList<String>>(psSize);
		ArrayList<String> thisComb;
		int binlength;

		for(int i= 1;i < psSize;++i){
			String bin = Integer.toBinaryString(i); //convert to binary

			thisComb = new ArrayList<String>(); //place to put one combination
			binlength = bin.length();
			for(int j= 0;j < binlength;j++){
				if(bin.charAt(binlength-(1+j)) == '1')
					thisComb.add(A.get(j));
			}
			ps.add(thisComb); //put this set in the answer list
		}

		return ps;
	}














}
