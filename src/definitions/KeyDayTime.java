package definitions;

public class KeyDayTime implements Comparable<KeyDayTime>{
public Integer Day = null;
public Integer Timeslot = null;

public KeyDayTime(int day_in, int time_in) {
	Day = day_in;
	Timeslot = time_in;
}

public int getDay() {
return Day;
}

public int getTimeslot() {
	return Timeslot;
}


@Override 
public int hashCode() {
	return Day.hashCode() + Timeslot.hashCode();
}

@Override 
public boolean equals(Object o) {
	if (o == this) {return true;}
	else if (o== null || !(o instanceof KeyDayTime)) {return false;}
	
	KeyDayTime kdt = KeyDayTime.class.cast(o);
	return (Day.equals(kdt.Day) && Timeslot.equals(kdt.Timeslot) );
}

public int compareTo(KeyDayTime kdt) {
	if (kdt == this) { return 0;};
	//Integer Day = null;
	Day.compareTo(kdt.getDay());
	if (Day != 0) {return Day;}
	
	//Integer Timeslot = null;
	Timeslot.compareTo(kdt.getTimeslot());
	if (Timeslot != 0) {return Timeslot;}
	
	return 0;
}

@Override
public String toString() {
	return Day.toString() + " " + Timeslot.toString();
}

}
