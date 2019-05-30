package jaicore.ml.tsc.util;

import jaicore.ml.tsc.distances.IScalarDistance;

/**
 * ScalarDistanceUtil
 */
public class ScalarDistanceUtil {

    public static IScalarDistance getAbsoluteDistance() {
        return (x, y) -> Math.abs(x - y);
    }

    public static IScalarDistance getSquaredDistance() {
        return (x, y) -> (x - y) * (x - y);
    }
}