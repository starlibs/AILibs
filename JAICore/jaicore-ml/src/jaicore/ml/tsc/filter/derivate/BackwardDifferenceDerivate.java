package jaicore.ml.tsc.filter.derivate;

/**
 * BackwardDifferenceDerivate f'(n) = f(n-1) - f(n)
 */
public class BackwardDifferenceDerivate extends ADerivateFilter {

    @Override
    protected double[] derivate(double[] T) {
        double[] derivate = new double[T.length - 1];

        for (int i = 1; i < T.length; i++) {
            derivate[i - 1] = T[i] - T[i - 1];
        }

        return derivate;
    }

    @Override
    protected double[] derivateWithBoundaries(double[] T) {
        double[] derivate = new double[T.length];

        for (int i = 1; i < T.length; i++) {
            derivate[i] = T[i] - T[i - 1];
        }

        derivate[0] = derivate[1];
        return derivate;
    }

}