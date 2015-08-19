package hilfsFunktionen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.HashSet;
import definitions.Course;
import definitions.Curriculum;
import definitions.KeyDayTime;
import definitions.KeyDayTimeRoom;
import definitions.Room;
import initialPopulation.populationGeneration;
import memeticAlgo.Algorithm;
import xmlParser.Parser;
/**
 * Created by alexandrubucur on 15.08.15.
 */

public class Funktionen {

    public static ArrayList<Integer> removeDuplicatesInteger(List<Integer> list) {

        // Store unique items in result.
        ArrayList<Integer> result = new ArrayList<>(list.size());

        // Record encountered Strings in HashSet.
        HashSet<Integer> set = new HashSet<>(list.size());

        // Loop over argument list.
        for (Integer item : list) {

            // If String is not in set, add it to the list and the set.
            if (!set.contains(item)) {
                result.add(item);
                set.add(item);
            }
        }
        return result;
    }

    public static ArrayList<Room> removeDuplicatesRoom(List<Room> list) {

        // Store unique items in result.
        ArrayList<Room> result = new ArrayList<>(list.size());

        // Record encountered Strings in HashSet.
        HashSet<Room> set = new HashSet<>(list.size());

        // Loop over argument list.
        for (Room item : list) {

            // If String is not in set, add it to the list and the set.
            if (!set.contains(item)) {
                result.add(item);
                set.add(item);
            }
        }
        return result;
    }

    public static boolean MapisEqual(Map<KeyDayTimeRoom, Course> map1, Map<KeyDayTimeRoom, Course> map2) {

        for (KeyDayTimeRoom i : map1.keySet()) {

            if (map2.keySet().contains(i)) {
                // if  ( (map1.get(i) == null && map2.get(i) != null)  ||  (map2.get(i) == null && map1.get(i) != null) || (map1.get(i).equals(map2.get(i))== false ) ) {
                if ((map1.get(i) == null && map2.get(i) != null) || (map2.get(i) == null && map1.get(i) != null)) {
                    return false;
                }
                if (map1.get(i) != null && map2.get(i) != null && (map1.get(i).courseID == map2.get(i).courseID) == false) {
                    return false;
                }
            } else {
                if ((map1.get(i) == null && map2.get(i) != null) || (map2.get(i) == null && map1.get(i) != null)) {
                    return false;
                }
            }

        }
        return true;
    }



    }



