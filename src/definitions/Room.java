/**
 * 
 */
package definitions;

/**
 * @author alexandrubucur
 *
 */
public class Room {
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

}
