package jaicore.search.algorithms.standard.uncertainty.paretosearch;

import java.util.Comparator;
import java.util.Random;

public class RandomComparator implements Comparator<ParetoNode> {

    /**
     * Randomly outputs a negative or positive integer. (Never zero).
     * @param first
     * @param second
     * @return negative iff first.n < second.n, 0 iff fist.n == second.n, positive iff first.n > second.n
     */
    public int compare(ParetoNode first, ParetoNode second) {
        Random random = new Random();
        int r = random.nextInt(2);  // This is either 0 or 1.
        if (r==0)
            return -1;
        else
            return 1;
    }

}
