package ai.libs.jaicore.planning.hierarchical.algorithms;

import org.api4.java.algorithm.IAlgorithmFactory;

import ai.libs.jaicore.basic.algorithm.reduction.AReducingSolutionIterator;
import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.interfaces.IGraphSearchBasedPlan;
import ai.libs.jaicore.search.model.other.SearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

/**
 *
 * @author fmohr
 *
 * @param <I1> Class of the planning problem
 * @param <O1> Class of the planning problem solutions (plans)
 * @param <I2> Class of the search problem
 * @param <O2> Class of the search problem solutions (paths)
 * @param <N> node type in search problem
 * @param <A> edge type in search problem
 */
public class GraphSearchBasedPlanningAlgorithm<I1, O1 extends IGraphSearchBasedPlan<N, A>, I2 extends GraphSearchInput<N, A>, O2 extends SearchGraphPath<N, A>, N, A>
extends AReducingSolutionIterator<I1, O1, I2, O2> {

	public GraphSearchBasedPlanningAlgorithm(final I1 problem, final AlgorithmicProblemReduction<I1, O1, I2, O2> problemTransformer, final IAlgorithmFactory<I2, O2, ?> baseFactory) {
		super(problem, problemTransformer, baseFactory);
	}
}
