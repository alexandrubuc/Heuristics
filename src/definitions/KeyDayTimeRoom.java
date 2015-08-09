/**
 * 
 */
package definitions;

/**
 * @author alexandrubucur
 *
 */
public class KeyDayTimeRoom implements Comparable<KeyDayTimeRoom>{
public Integer Day = null;
public Integer Timeslot = null;
public Room assignedRoom = null;

public KeyDayTimeRoom(int day_in, int time_in, Room room_in) {
	Day = day_in;
	Timeslot = time_in;
	assignedRoom = room_in;
}

public int getDay() {
return Day;
}

public int getTimeslot() {
	return Timeslot;
}

public Room getRoom() {
	return assignedRoom;
}

@Override 
public int hashCode() {
	return Day.hashCode() + Timeslot.hashCode() + assignedRoom.hashCode();
}

@Override 
public boolean equals(Object o) {
	if (o == this) {return true;}
	else if (o== null || !(o instanceof KeyDayTimeRoom)) {return false;}
	
	KeyDayTimeRoom kdtr = KeyDayTimeRoom.class.cast(o);
	return (Day.equals(kdtr.Day) && Timeslot.equals(kdtr.Timeslot) && assignedRoom.roomID.equals(kdtr.assignedRoom.roomID) && assignedRoom.roomCapacity.equals(kdtr.assignedRoom.roomCapacity));
}

public int compareTo(KeyDayTimeRoom kdtr) {
	if (kdtr == this) { return 0;};
	//Integer Day = null;
	Day.compareTo(kdtr.getDay());
	if (Day != 0) {return Day;}
	
	//Integer Timeslot = null;
	Timeslot.compareTo(kdtr.getTimeslot());
	if (Timeslot != 0) {return Timeslot;}
	
	if (assignedRoom.roomCapacity.compareTo(kdtr.getRoom().roomCapacity) != 0) {return 1;}
	if (assignedRoom.roomID.compareTo(kdtr.getRoom().roomID) != 0) {return 1;}
	return 0;
}

@Override
public String toString() {
	return Day.toString() + " " + Timeslot.toString() + " " + assignedRoom.roomID + " " + assignedRoom.roomCapacity;
}

}
