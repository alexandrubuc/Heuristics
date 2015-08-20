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
	@Override
	public int hashCode() {
		return 4353*courseID.hashCode() + 234*teacherID.hashCode() + 45645*numberOfLectures.hashCode() + numberOfUnassignedLectures.hashCode() + minWorkDays.hashCode() + students.hashCode() + doubleLectures.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {return true;}
		if (this == null && o == null) {return true;}
		else if (o== null || !(o instanceof Course)) {
			return false;}

		Course curso = Course.class.cast(o);
		return (courseID.equals(curso.courseID) && teacherID.equals(curso.teacherID) && numberOfLectures.equals(curso.numberOfLectures) && numberOfUnassignedLectures.equals(curso.numberOfUnassignedLectures) &&minWorkDays.equals(curso.minWorkDays) && students.equals(curso.students) && doubleLectures.equals(curso.doubleLectures));
	}

}
