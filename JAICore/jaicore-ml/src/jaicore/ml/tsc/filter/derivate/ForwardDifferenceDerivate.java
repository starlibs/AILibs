package jaicore.ml.tsc.filter.derivate;

/**
 * Filter that calculate the Forward Difference derivate. The Forward Difference
 * derivate <code>T'</code> for a time series
 * <code>T = {T(0), T(1), T(2), .., T(n)}<code> is defined as <code>T'(i) = T(i+1) - T(i)</code>
 * for <code>i = 0 to n-1</code>. When padded, <code>T'(n) = T'(n-1)</code>.
 * 
 * @author fischor
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