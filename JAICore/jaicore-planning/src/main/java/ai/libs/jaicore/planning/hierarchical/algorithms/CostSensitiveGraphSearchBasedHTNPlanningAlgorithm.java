package ai.libs.jaicore.planning.hierarchical.algorithms;

import org.api4.java.algorithm.IAlgorithmFactory;
import org.slf4j.Logger;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.classical.problems.strips.Operation;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedGraphSearchBasedPlan;
import ai.libs.jaicore.planning.hierarchical.problems.htn.IHTNPlanningProblem;
import ai.libs.jaicore.planning.hierarchical.problems.stn.Method;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;

/**
 *
 * @author fmohr
 *
 * @param <P>
 *            class of the HTN planning problem
 * @param <S>
 *            class of the graph search problem input to which the HTN problem is reduced
 * @param <N>
 *            class of the nodes in the search problem
 * @param <A>
 *            class of the edges in the search problem
 * @param <V>
 *            evaluation of solutions
 */
public class CostSensitiveGraphSearchBasedHTNPlanningAlgorithm<P extends IHTNPlanningProblem, S extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>>
extends CostSensitiveGraphSearchBasedPlanningAlgorithm<P, S, N, A, V> {

	public CostSensitiveGraphSearchBasedHTNPlanningAlgorithm(final P problem, final AlgorithmicProblemReduction<P, IEvaluatedGraphSearchBasedPlan<N, A, V>, S, EvaluatedSearchGraphPath<N, A, V>> problemTransformer,
			final IAlgorithmFactory<S, EvaluatedSearchGraphPath<N, A, V>, ?> baseFactory) {
		super(problem, problemTransformer, baseFactory);
	}

	@Override
	public void runPreCreationHook() {
		super.runPreCreationHook();
		Logger logger = this.getLogger();
		logger.info("Starting HTN planning process.");
		if (logger.isDebugEnabled()) {
			StringBuilder opSB = new StringBuilder();
			for (Operation op : this.getInput().getDomain().getOperations()) {
				opSB.append("\n\t\t");
				opSB.append(op);
			}
			StringBuilder methodSB = new StringBuilder();
			for (Method method : this.getInput().getDomain().getMethods()) {
				methodSB.append("\n\t\t");
				methodSB.append(method);
			}
			logger.debug("The HTN problem is defined as follows:\n\tOperations:{}\n\tMethods:{}", opSB, methodSB);
		}
	}
}
