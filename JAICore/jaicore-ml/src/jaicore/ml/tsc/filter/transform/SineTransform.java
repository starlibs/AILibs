package jaicore.ml.tsc.filter.transform;

/**
 * Calculates the sine transform of a time series. For this implementation, the
 * definition as given in "Non-isometric transforms in time series
 * classification using DTW" by Tomasz Gorecki and Maciej Luczak (2014) is used.
 * 
 * The sine transform <code>f = {f(k): k = 1 to n}</code> of a time series
 * <code>T = {T(i): i = 1 to n }</code> is defined as
 * <code>f(k) = sum_{i=1}^{n} T(i) * sin[(PI/n)*(i-0.5)*k]</code>.
 * 
 * @author fischor
 */
public class SineTransform extends ATransformFilter {

    @Override
    public double[] transform(double[] input) {
        double n = input.length;
        double[] cosinusTransform = new double[input.length];
        for (int k = 0; k < n; k++) {
            // Sum over all points of the input.
            double sum = 0;
            for (int i = 0; i < n; i++) {
                // Make (i-1) to (i+1) and k to (k+1) because of zero-indexing.
                sum += input[i] * Math.sin((Math.PI / n) * (i + 0.5) * (k + 1));
            }
            cosinusTransform[k] = sum;
        }
        return cosinusTransform;
    }

}