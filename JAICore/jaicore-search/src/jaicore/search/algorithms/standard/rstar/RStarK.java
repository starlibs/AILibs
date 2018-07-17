package jaicore.search.algorithms.standard.rstar;

/**
 * k-Values (Priorities used for expansion from open.)
 */
public class RStarK implements Comparable<RStarK>{

    boolean avoid;
    double f;

    RStarK(boolean avoid, double f) {
        this.avoid = avoid;
        this.f = f;
    }

    @Override
    /**
     * Compare. Higher priority means lower RStarK.
     * @return
     */
    public int compareTo(RStarK o) {
        // Compare first AVOID flag.
        if (!this.avoid && o.avoid) {
            return +1;
        }
        if (this.avoid && !o.avoid) {
            return -1;
        }
        // Then compare f-values.
        return Double.compare(this.f, o.f);
    }
}
