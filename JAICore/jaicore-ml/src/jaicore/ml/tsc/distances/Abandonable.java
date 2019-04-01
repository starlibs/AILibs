package jaicore.ml.tsc.distances;

/**
 * Interface for Distance measures that can make use of the Early Abandon
 * technique.
 * 
 * With Early Abandon the distance calculation will be abandoned once the
 * calculation has exceed a bestSoFar distance.
 */
public interface Abandonable {

    /**
     * Setter for the best-so-far value.
     * 
     * @param limit The limit.
     */
    public void setBestSoFar(double limit);

    /**
     * Getter for the best-so-far value.
     * 
     * @return The limit.
     */
    public double getBestSoFar();
}