package ai.libs.jaicore.planning.hierarchical.algorithms;

import org.api4.java.algorithm.IAlgorithmFactory;
import org.api4.java.algorithm.IOptimizationAlgorithm;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

/**
 *
 * @author fmohr
 *
 * @param <I1> Class of the planning problem
 * @param <I2> Class of the search problem
 * @param <N> node type in search problem
 * @param <A> edge type in search problem
 * @param <V> cost associated with plans (and paths)
 */
public class CostSensitiveGraphSearchBasedPlanningAlgorithm<I1, I2 extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>>
extends GraphSearchBasedPlanningAlgorithm<I1, IEvaluatedGraphSearchBasedPlan<N, A, V>, I2, EvaluatedSearchGraphPath<N, A, V>, N, A> implements IOptimizationAlgorithm<I1, IEvaluatedGraphSearchBasedPlan<N, A, V>, V> {

	public CostSensitiveGraphSearchBasedPlanningAlgorithm(final I1 problem, final AlgorithmicProblemReduction<I1, IEvaluatedGraphSearchBasedPlan<N, A, V>, I2, EvaluatedSearchGraphPath<N, A, V>> problemTransformer, final IAlgorithmFactory<I2, EvaluatedSearchGraphPath<N, A, V>, ?> baseFactory) {
		super(problem, problemTransformer, baseFactory);
	}
}
