package jaicore.ml.tsc.filter.transform;

/**
 * Calculates the Hilbert transform of a time series. For this implementation,
 * the definition as given in "Non-isometric transforms in time series
 * classification using DTW" by Tomasz Gorecki and Maciej Luczak (2014) is used.
 * 
 * The Hilbert transform <code>f = {f(k): k = 1 to n}</code> of a time series
 * <code>T = {T(i): i = 1 to n }</code> is defined as
 * <code>f(k) = sum_{i=1, i!=k}^{n} f(i) / (k-i)</code>.
 * 
 * @author fischor
 */
public class HilbertTransform extends ATransformFilter {

    @Override
    public double[] transform(double[] input) {
        double n = input.length;
        double[] hilbertTransform = new double[input.length];
        for (int k = 0; k < n; k++) {
            // Sum over all points of the input.
            double sum = 0;
            for (int i = 0; i < n; i++) {
                if (i != k) {
                    sum += input[i] / (k - i);
                }
            }
            hilbertTransform[k] = sum;
        }
        return hilbertTransform;
    }

}