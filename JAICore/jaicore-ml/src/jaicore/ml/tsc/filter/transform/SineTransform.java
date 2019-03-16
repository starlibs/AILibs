package jaicore.ml.tsc.filter.transform;

/**
 * SineTransform
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