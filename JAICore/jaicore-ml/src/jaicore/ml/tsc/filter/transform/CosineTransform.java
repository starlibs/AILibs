package jaicore.ml.tsc.filter.transform;

/**
 * CosineTransform
 */
public class CosineTransform extends ATransformFilter {

    @Override
    public double[] transform(double[] input) {
        double n = input.length;
        double[] cosinusTransform = new double[input.length];
        for (int k = 0; k < n; k++) {
            // Sum over all points of the input.
            double sum = 0;
            for (int i = 0; i < n; i++) {
                // Make (i - 0.5) to (i + 0.5) and (k-1) to k because of zero-indexing.
                sum += input[i] * Math.cos((Math.PI / n) * (i + 0.5) * k);
            }
            cosinusTransform[k] = sum;
        }
        return cosinusTransform;
    }

}