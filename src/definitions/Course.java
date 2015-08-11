/**
 * 
 */
package definitions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alexandrubucur
 *
 */
public class Course {
public Integer courseID;
public Integer teacherID;
public Integer numberOfLectures;
public Integer numberOfUnassignedLectures;
public Integer minWorkDays;
public Integer students;
public Boolean doubleLectures;

public List<Curriculum> belongsToCurricula = new ArrayList<>();

public List<KeyDayTime> constraintsTimeslot = new ArrayList<>();
public List<Room> constraintsRoom = new ArrayList<>();


//Constructor
public Course(int id, int teach, int num_of_lec, int minDays,int students_,Boolean doubleLectures_) {
	courseID = id;
	teacherID = teach;
	numberOfLectures = num_of_lec;
	numberOfUnassignedLectures = numberOfLectures;
	minWorkDays = minDays;
	students = students_;
	doubleLectures = doubleLectures_;
}
}
