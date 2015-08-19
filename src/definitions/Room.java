/**
 * 
 */
package definitions;

/**
 * @author alexandrubucur
 *
 */
public class Room implements Comparable<Room> {
String roomID;
Integer roomCapacity;
String Building;

public String getRoomID () {return roomID;}
public Integer getRoomCapacity () {return roomCapacity;}
public String getBuilding() {return Building;}

//Constructor
public Room(String id, int capacity, String building) {
	roomID = id;
	roomCapacity = capacity;
	Building = building;
}

	@Override
	public int hashCode() {
		return ((String)roomID).hashCode() + ((Integer)roomCapacity).hashCode() +((String) Building).hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {return true;}
		else if (o== null || !(o instanceof Room)) {return false;}

		Room roomie = Room.class.cast(o);
		return ((roomID.equals(((Room) o).getRoomID())) && (Building.equals(((Room) o).getBuilding())) &&  (roomCapacity.equals(((Room) o).roomCapacity)) );
	}

public int compareTo(Room roomie) {
		if (roomie == this) { return 0;};
		//Integer Day = null;
		int result = roomID.compareTo(roomie.getRoomID());
		if (result != 0) {return result;}

		//Integer Timeslot = null;
		int resultado = roomCapacity.compareTo(roomie.getRoomCapacity());
		if (resultado != 0) {return resultado;}

		int resulte = Building.compareTo(roomie.getBuilding());
		if (resulte != 0) {return resulte;}
		return 0;
	}

}
