package jaicore.ml.tsc.util;

import java.util.List;

/**
 * EvaluationUtil
 */
public class EvaluationUtil {

    /**
     * Calculates the accurracy.
     * 
     * @param predictions List of predicitions as returned by a classifier.
     * @param targets     Array of targets as contained in the dataset.
     * @return Accuracy.
     */
    public static double accuracy(List<Integer> predictions, int[] targets) {
        // Parameter checks.
        if (predictions.size() != targets.length) {
            throw new IllegalArgumentException("Predicitons and targets have to be same same size/length.");
        }
        // Calculation.
        int correct = 0;
        for (int i = 0; i < predictions.size(); i++) {
            if (predictions.get(i) == targets[i]) {
                correct++;
            }
        }
        return (double) correct / predictions.size();
    }
}