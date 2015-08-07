/**
 * 
 */
package definitions;

/**
 * @author alexandrubucur
 *
 */
public class Room {
Integer roomID;
Integer roomCapacity;

public Integer getRoomID () {return roomID;}
public Integer getRoomCapacity () {return roomCapacity;}

//Constructor
public Room(int id, int capacity) {
	roomID = id;
	roomCapacity = capacity;
}

}
