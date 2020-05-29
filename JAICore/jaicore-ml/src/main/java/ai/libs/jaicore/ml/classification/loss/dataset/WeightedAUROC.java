package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.Arrays;
import java.util.List;

import ai.libs.jaicore.ml.classification.loss.ConfusionMatrix;

/**
 * Computes the AUROC weighted by class sizes, that is, it first computes the size of each class,
 * then computes AUROC in a one-vs-rest fashion and balances the final score proportional to the
 * size of each class.
 *
 * @author mwever
 */
public class WeightedAUROC extends AHomogeneousPredictionPerformanceMeasure<Object> {

	@Override
	public double score(final List<?> expected, final List<?> actual) {
		ConfusionMatrix cmat = new ConfusionMatrix(expected, actual);
		int[] classCounts = Arrays.stream(cmat.getConfusionMatrix()).mapToInt(x -> Arrays.stream(x).sum()).toArray();
		double sum = 0;
		for (int i = 0; i < classCounts.length; i++) {
			if (classCounts[i] > 0) {
				sum += new AreaUnderROCCurve(cmat.getObjectIndex().get(i)).score(expected, actual) * classCounts[i];
			}
		}
		return sum / Arrays.stream(classCounts).sum();
	}

}
