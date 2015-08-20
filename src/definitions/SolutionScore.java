package definitions;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by alexandrubucur on 19.08.15.
 */
public class SolutionScore {
    Map<KeyDayTimeRoom,Course> solution;
    int score;

    public Map<KeyDayTimeRoom,Course> getSolution() {return solution;}
    public int getScore() {return score;}

    public SolutionScore(Map<KeyDayTimeRoom,Course> sol_in, int score_in) {
        solution = new HashMap<>(sol_in.size());
        for (Map.Entry<KeyDayTimeRoom,Course> entry : sol_in.entrySet()) {
            solution.put(entry.getKey(),entry.getValue());
        }
        score = score_in;
    }

}
