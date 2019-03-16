package jaicore.ml.tsc.filter.transform;

/**
 * HilbertTransform
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