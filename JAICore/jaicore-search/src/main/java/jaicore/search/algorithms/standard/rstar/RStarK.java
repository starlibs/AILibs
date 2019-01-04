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
     * Compare to k-values i.e. provide a natural ordering for them.
     * E.g.: [false, 0.9] < [false, 2.2] < [true, 0.1] < [true, 2.0]
     *
     * @return -1 if this < o, 0 iff this == o, +1 iff this > 0
     */
    public int compareTo(RStarK o) {
        // Compare first AVOID flag.
        if (!this.avoid && o.avoid) {
            return -1;
        }
        if (this.avoid && !o.avoid) {
            return +1;
        }
        // Then compare f-values.
        return Double.compare(this.f, o.f);
    }

    @Override
    public String toString() {
        return String.format("[%b, %g]", this.avoid, this.f);
    }
}
