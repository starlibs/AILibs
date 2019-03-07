package jaicore.ml.tsc.filter.derivate;

/**
 * ForwardDifferenceDerivate f'(n) = f(n+1) - f(n)
 */
public class ForwardDifferenceDerivate extends ADerivateFilter {

    public ForwardDifferenceDerivate() {
        super();
    }

    public ForwardDifferenceDerivate(boolean withBoundaries) {
        super(withBoundaries);
    }

    @Override
    protected double[] derivate(double[] T) {
        double[] derivate = new double[T.length - 1];

        for (int i = 0; i < T.length - 1; i++) {
            derivate[i] = T[i + 1] - T[i];
        }

        return derivate;
    }

    @Override
    protected double[] derivateWithBoundaries(double[] T) {
        double[] derivate = new double[T.length];

        for (int i = 0; i < T.length - 1; i++) {
            derivate[i] = T[i + 1] - T[i];
        }

        derivate[T.length - 1] = derivate[T.length - 2];
        return derivate;
    }

}