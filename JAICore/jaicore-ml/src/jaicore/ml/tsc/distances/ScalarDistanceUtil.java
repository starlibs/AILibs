package jaicore.ml.tsc.distances;

/**
 * ScalarDistanceUtil
 */
public class ScalarDistanceUtil {

    public static IScalarDistance getAbsoluteDistance() {
        return (x, y) -> Math.abs(x - y);
    }

    public static IScalarDistance getEuclideanDistance() {
        return (x, y) -> Math.sqrt(x * x - y * y);
    }
}