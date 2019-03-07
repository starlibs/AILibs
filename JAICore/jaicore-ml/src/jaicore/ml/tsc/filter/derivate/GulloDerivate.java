package jaicore.ml.tsc.filter.derivate;

/**
 * Calculates the derivative of a timeseries as described first by Gullo et. al
 * (2009).
 */
public class GulloDerivate extends ADerivateFilter {

    @Override
    protected double[] derivate(double[] T) {
        double[] derivate = new double[T.length - 1];

        for (int i = 1; i < T.length; i++) {
            derivate[i - 1] = T[i + 1] - T[i - 1] / 2;
        }

        return derivate;
    }

    @Override
    protected double[] derivateWithBoundaries(double[] T) {
        double[] derivate = new double[T.length];

        for (int i = 1; i < T.length; i++) {
            derivate[i] = T[i + 1] - T[i - 1] / 2;
        }

        derivate[0] = derivate[1];
        return derivate;
    }
}