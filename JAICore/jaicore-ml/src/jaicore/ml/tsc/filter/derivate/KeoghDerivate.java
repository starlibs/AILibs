package jaicore.ml.tsc.filter.derivate;

/**
 * Calculates the derivative of a timeseries as described first by Keogh and
 * Pazzani (2001).
 * 
 * The Keogh derivate <code>T'</code> for a time series
 * <code>T = {T(0), T(1), T(2), .., T(n)}<code> is defined as <code>T'(i) = (T(i) - T(i-1) + (T(i+1) - T(i-1)) / 2)) / 2</code>
 * for <code>i = 1 to n-1</code>. When padded, <code>T'(0) = T'(1)</code> and
 * <code>T'(n) = T'(n-1)</code>.
 * 
 * @author fischor
 */
public class KeoghDerivate extends ADerivateFilter {

    public KeoghDerivate() {
        super();
    }

    public KeoghDerivate(boolean withBoundaries) {
        super(withBoundaries);
    }

    @Override
    protected double[] derivate(double[] T) {
        double[] derivate = new double[T.length - 2];

        for (int i = 1; i < T.length - 1; i++) {
            derivate[i - 1] = ((T[i] - T[i - 1]) + (T[i + 1] - T[i - 1]) / 2) / 2;
        }

        return derivate;
    }

    @Override
    protected double[] derivateWithBoundaries(double[] T) {
        double[] derivate = new double[T.length];

        for (int i = 1; i < T.length - 1; i++) {
            derivate[i] = ((T[i] - T[i - 1]) + (T[i + 1] - T[i - 1]) / 2) / 2;
        }

        derivate[0] = derivate[1];
        derivate[T.length - 1] = derivate[T.length - 2];

        return derivate;
    }

}