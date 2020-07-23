package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.ai.ml.classification.singlelabel.evaluation.ISingleLabelClassification;

import ai.libs.jaicore.basic.Maps;

/**
 * Computes the AUROC weighted by class sizes, that is, it first computes the size of each class,
 * then computes AUROC in a one-vs-rest fashion and balances the final score proportional to the
 * size of each class.
 *
 * @author mwever
 */
public class WeightedAUROC extends ASingleLabelClassificationPerformanceMeasure {

	@Override
	public double score(final List<? extends Integer> expected, final List<? extends ISingleLabelClassification> predicted) {
		Map<Integer, Integer> classCountMap = new HashMap<>();
		expected.stream().forEach(x -> Maps.increaseCounterInMap(classCountMap, x));
		double sum = 0;
		for (Entry<Integer, Integer> posClassEntry : classCountMap.entrySet()) {
			sum += new AreaUnderROCCurve(posClassEntry.getKey()).score(expected, predicted) * posClassEntry.getValue();
		}
		return sum / classCountMap.values().stream().mapToInt(x -> x).sum();
	}

}
