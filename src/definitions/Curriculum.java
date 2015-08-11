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
public class Curriculum {
	public Integer curriculumID;
	public List<Course> coursesThatBelongToCurr = new ArrayList<Course>();
	
	public Curriculum(Integer currID, List<Course> list) {
		this.curriculumID = currID;
		this.coursesThatBelongToCurr = list;
	}
}
