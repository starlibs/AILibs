package jaicore.ml.tsc.distances;

/**
 * Functional interface for the distance of two scalars.
 * 
 * @author fischor
 */
public interface IScalarDistance {

    /**
     * Calculates the distance between two scalars.
     */
    public double distance(double x, double y);
}