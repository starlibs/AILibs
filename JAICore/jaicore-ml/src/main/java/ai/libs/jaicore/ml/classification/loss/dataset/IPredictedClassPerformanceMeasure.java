package ai.libs.jaicore.ml.classification.loss.dataset;

import java.util.List;

public interface IPredictedClassPerformanceMeasure {

	public double loss(List<? extends Integer> expected, List<? extends Integer> predicted);

	public double score(List<? extends Integer> expected, List<? extends Integer> predicted);

}
