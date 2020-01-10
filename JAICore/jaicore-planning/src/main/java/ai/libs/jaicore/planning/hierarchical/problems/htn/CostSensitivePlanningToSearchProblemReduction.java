package ai.libs.jaicore.planning.hierarchical.problems.htn;



import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IEvaluatedPath;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import ai.libs.jaicore.planning.core.EvaluatedPlan;
import ai.libs.jaicore.planning.core.interfaces.IEvaluatedPlan;
import ai.libs.jaicore.planning.core.interfaces.IPlan;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.GraphSearchInput;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;

public class CostSensitivePlanningToSearchProblemReduction<N, A, V extends Comparable<V>, I1 extends IHTNPlanningProblem, I2 extends GraphSearchWithPathEvaluationsInput<N, A, V>, O2 extends IEvaluatedPath<N, A, V>>
implements IHierarchicalPlanningToGraphSearchReduction<N, A, CostSensitiveHTNPlanningProblem<I1, V>, IEvaluatedPlan<V>, I2, O2> {

	private final IHierarchicalPlanningToGraphSearchReduction<N, A, I1, IPlan, GraphSearchInput<N, A>, ILabeledPath<N, A>> baseReduction;
	private final AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, I2, O2> forwardReduction;

	public CostSensitivePlanningToSearchProblemReduction(final IHierarchicalPlanningToGraphSearchReduction<N, A, I1, IPlan, GraphSearchInput<N, A>, ILabeledPath<N, A>> baseReduction,
			final AlgorithmicProblemReduction<? super GraphSearchWithPathEvaluationsInput<N, A, V>, ? super EvaluatedSearchGraphPath<N, A, V>, I2, O2> forwardReduction) {
		super();
		this.baseReduction = baseReduction;
		this.forwardReduction = forwardReduction;
	}

	/**
	 * This method operates in three steps:
	 * 1) it derives a general graph search problem from the given planning problem
	 * 2) it combines the obtained graph search problem with a path evaluation function into a GrahhSearchWithPathEvaluationsInput
	 * 3) it derives a potentially more informed GraphSearchInput
	 *
	 * The last process is called the forward reduction. The output does not change.
	 */
	@Override
	public I2 encodeProblem(final CostSensitiveHTNPlanningProblem<I1, V> problem) {
		return this.forwardReduction.encodeProblem(new GraphSearchWithPathEvaluationsInput<>(this.baseReduction.encodeProblem(problem.getCorePlanningProblem()), new Evaluator<>(problem.getPlanEvaluator(), this.baseReduction)));
	}

	@Override
	public IEvaluatedPlan<V> decodeSolution(final O2 solution) {
		return new EvaluatedPlan<>(this.baseReduction.decodeSolution(solution), solution.getScore());
	}

	private static class Evaluator<N, A, V extends Comparable<V>> implements IObjectEvaluator<ILabeledPath<N, A>, V>, ILoggingCustomizable {

		private Logger logger = LoggerFactory.getLogger(Evaluator.class);
		private final IObjectEvaluator<IPlan, V> planEvaluator;
		private final IHierarchicalPlanningToGraphSearchReduction<N, A, ?, IPlan, GraphSearchInput<N, A>, ILabeledPath<N, A>> baseReduction;

		public Evaluator(final IObjectEvaluator<IPlan, V> planEvaluator, final IHierarchicalPlanningToGraphSearchReduction<N, A, ?, IPlan, GraphSearchInput<N, A>, ILabeledPath<N, A>> baseReduction) {
			super();
			this.planEvaluator = planEvaluator;
			this.baseReduction = baseReduction;
		}

		@Override
		public String getLoggerName() {
			return this.logger.getName();
		}

		@Override
		public void setLoggerName(final String name) {
			this.logger = LoggerFactory.getLogger(name);
			if (this.planEvaluator instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) this.planEvaluator).setLoggerName(name + ".pe");
				this.logger.info("Setting logger of plan evaluator {} to {}.pe", this.planEvaluator.getClass().getName(), name);
			}
			else {
				this.logger.info("Plan evaluator {} is not configurable for logging, so not configuring it.", this.planEvaluator);
			}
		}

		@Override
		public V evaluate(final ILabeledPath<N, A> solutionPath) throws InterruptedException, ObjectEvaluationFailedException {
			this.logger.info("Forwarding evaluation to plan evaluator {}", this.planEvaluator.getClass().getName());
			return this.planEvaluator.evaluate(this.baseReduction.decodeSolution(solutionPath));
		}

	}
}
