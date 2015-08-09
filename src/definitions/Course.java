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
public Integer TeacherID;
public Integer numberOfLectures;
public Integer numberOfUnassignedLectures;
public Integer minWorkDays;

public List<Curriculum> belongsToCurricula = new ArrayList<>();

//Constructor
public Course(int id, int teach, int num_of_lec, List<Curriculum> curr) {
	courseID = id;
	TeacherID = teach;
	numberOfLectures = num_of_lec;
	belongsToCurricula = curr;
}
}
