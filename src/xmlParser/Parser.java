package xmlParser;


import initialPopulation.populationGeneration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import definitions.Course;
import definitions.Curriculum;
import definitions.KeyDayTime;
import definitions.KeyDayTimeRoom;
import definitions.Room;

import javax.xml.parsers.ParserConfigurationException; 



public class Parser {


	private static String xmlFile;
	private static File fXmlFile;
	private static DocumentBuilderFactory dbFactory;
	private static DocumentBuilder dBuilder;
	private static Document doc;
	private static Element e;
	private static NodeList nList;


	private static int numberofcourses, numberofrooms, numberofperiods, numberofdays, numberofcurricula, numberofcoursespC, numberofS;

	private static Boolean doubleLectures;
	private static Integer numberStudents, courseID, teacherID, minDays, numOfLectures;
	
	
	
	
	public static void setxmlFile(String xmlFile_){
		try {
			xmlFile = xmlFile_;
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
	public static void getData(){ 

	    getAvailableTimeslots();
		getRooms();
		getCourses();
		getCurricula();
		//for each course in listCourses, get the list of curricula to which the course belongs 
		populationGeneration.listCourses.stream().forEach(course -> course.belongsToCurricula = populationGeneration.currForCourse(course));
		// still get the course constraints
		getConstraints();
		
	}

	private static void getAvailableTimeslots() {
		nList = doc.getElementsByTagName("descriptor");
		e = (Element) nList.item(0);

		nList = doc.getElementsByTagName("periods_per_day");
		e = (Element) nList.item(0);
		numberofperiods = Integer.parseInt(e.getAttribute("value"));

		nList = doc.getElementsByTagName("days");
		e = (Element) nList.item(0);
		numberofdays = Integer.parseInt(e.getAttribute("value"));

		populationGeneration.timeslotsPerDay = numberofperiods;
		populationGeneration.totalDays = numberofdays;

	}


	private static void getCourses() {
		nList = doc.getElementsByTagName("courses");
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("course");
		numberofcourses = nList.getLength();

		for(int i = 0; i < numberofcourses;i++){
			e = (Element) nList.item(i);
			
			boolean doubleLectures;
			if(e.getAttribute("double_lectures") == "yes")
				doubleLectures  = true;
			else	
				doubleLectures = false;

			numberStudents = Integer.parseInt(e.getAttribute("students"));
			courseID = Integer.parseInt((e.getAttribute("id").substring(1)));
			teacherID = Integer.parseInt(e.getAttribute("teacher").substring(1));
			minDays = Integer.parseInt(e.getAttribute("min_days"));
			numOfLectures = Integer.parseInt(e.getAttribute("lectures"));
			
			Course CtoAdd = new Course(courseID, teacherID, numOfLectures, minDays, numberStudents, doubleLectures);
			populationGeneration.listCourses.add(CtoAdd);
		}
	}

	private static void getRooms(){
		nList = doc.getElementsByTagName("rooms");
		String ID;
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("room");
		int numberofrooms = nList.getLength();

		String building;
		int size;

		for(int i = 0; i < numberofrooms;i++){
			e = (Element) nList.item(i);

			building = e.getAttribute("building");
			ID = e.getAttribute("id");
			size = Integer.parseInt(e.getAttribute("size"));

			populationGeneration.listRooms.add(new Room(ID, size, building));
		}

	}

	private static void getCurricula(){
		nList = doc.getElementsByTagName("curricula");
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("curriculum");
		numberofcurricula = nList.getLength();

		NodeList nCourses;
		Integer currID;

		for(int i = 0; i < numberofcurricula;i++){
			e = (Element) nList.item(i);
			currID = Integer.parseInt(e.getAttribute("id").substring(1));

			//get all courseIds, which refer to the curriculum
			nCourses = e.getElementsByTagName("course");
			numberofcoursespC = nCourses.getLength();
			List<Course> courseincurr = new ArrayList<>(numberofcoursespC);
			
			for(int j= 0; j < numberofcoursespC;j++){ 
				e = (Element) nCourses.item(j);
				int courseid = Integer.parseInt(e.getAttribute("ref").substring(1));
				//get the courses from the course list and add it to the curriculum
				courseincurr.add(populationGeneration.listCourses.stream().filter( course -> course.courseID == courseid).findAny().get());
			}

			populationGeneration.listCurricula.add(new Curriculum(currID,courseincurr)); 

		}
	}

	private static void getConstraints() {
		
		nList = doc.getElementsByTagName("constraints");
		e = (Element) nList.item(0);
		nList = e.getElementsByTagName("constraint");
		int numberofconstraints = nList.getLength();
		int numberoftimeslots;

		NodeList nList2;


		for(int i = 0; i < numberofconstraints;i++){
			e = (Element) nList.item(i);
			Integer course_id_ = Integer.parseInt(e.getAttribute("course").substring(1));

			//get eligible rooms
			if(e.getAttribute("type").equals("room")){
				nList2 = e.getElementsByTagName("room");
				numberofrooms = nList2.getLength();

				for(int j= 0; j < numberofrooms;j++){
					e = (Element) nList2.item(j);
					Room room = populationGeneration.listRooms.stream().filter(camera -> camera.getRoomID() == e.getAttribute("ref")).findAny().get();
					populationGeneration.listCourses.stream().filter(course -> course.courseID == course_id_).findAny().get().constraintsRoom.add(room);
				}

			}//get eligible timeslots
			else if(e.getAttribute("type").equals("period")){
				nList2 = e.getElementsByTagName("timeslot");
				numberoftimeslots = nList2.getLength();

				for(int j= 0; j < numberoftimeslots;j++){
					e = (Element) nList2.item(j);
					KeyDayTime kdt = new KeyDayTime( Integer.parseInt(e.getAttribute("day")),Integer.parseInt(e.getAttribute("period")));
					populationGeneration.listCourses.stream().filter(course -> course.courseID == course_id_).findAny().get().constraintsTimeslot.add(kdt);
				}
			}

		}

	}

	
	















}
