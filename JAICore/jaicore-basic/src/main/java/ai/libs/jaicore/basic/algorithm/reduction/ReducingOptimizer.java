package ai.libs.jaicore.basic.algorithm.reduction;

import org.api4.java.algorithm.IOptimizationAlgorithm;
import org.api4.java.algorithm.IOptimizationAlgorithmFactory;
import org.api4.java.common.attributedobjects.ScoredItem;

public class ReducingOptimizer<I1, O1 extends ScoredItem<V>, I2, O2 extends ScoredItem<V>, V extends Comparable<V>> extends AReducingSolutionIterator<I1, O1, I2, O2> implements IOptimizationAlgorithm<I1,O1, V> {

	/* algorithm inputs */
	public ReducingOptimizer(final I1 problem, final AlgorithmicProblemReduction<I1, O1, I2, O2> problemTransformer, final IOptimizationAlgorithmFactory<I2, O2, V, ?> baseFactory) {
		super(problem, problemTransformer, baseFactory);
	}
}
