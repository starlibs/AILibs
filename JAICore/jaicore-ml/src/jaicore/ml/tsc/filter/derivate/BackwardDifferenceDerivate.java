package jaicore.ml.tsc.filter.derivate;

/**
 * Filter that calculate the Backward Difference derivate. The Backward
 * Difference derivate <code>t'</code> for a time series
 * <code>T = {T(1), T(2), .., T(n)}<code> is defined as <code>T'(i) = T(i) - T(i-1)</code>
 * for <code>i = 1 to n</code>. When padded, <code>T'(0) = T'(1)</code>.
 * 
 * @author fischor
 */
public class BackwardDifferenceDerivate extends ADerivateFilter {

    public BackwardDifferenceDerivate() {
        super();
    }

    public BackwardDifferenceDerivate(boolean withBoundaries) {
        super(withBoundaries);
    }

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