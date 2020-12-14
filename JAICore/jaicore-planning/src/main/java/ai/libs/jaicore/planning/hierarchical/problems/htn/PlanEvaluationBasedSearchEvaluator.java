package ai.libs.jaicore.planning.hierarchical.problems.htn;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.planning.core.interfaces.IPlan;

public class PlanEvaluationBasedSearchEvaluator<N, A, V extends Comparable<V>> implements IPathEvaluator<N, A, V>, ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(PlanEvaluationBasedSearchEvaluator.class);
	private final IObjectEvaluator<IPlan, V> planEvaluator;
	private final IHierarchicalPlanningToGraphSearchReduction<N, A, ?, IPlan, ? extends IPathSearchInput<N, A>, ILabeledPath<N, A>> baseReduction;

	public PlanEvaluationBasedSearchEvaluator(final IObjectEvaluator<IPlan, V> planEvaluator, final IHierarchicalPlanningToGraphSearchReduction<N, A, ?, IPlan, ? extends IPathSearchInput<N, A>, ILabeledPath<N, A>> baseReduction) {
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
	public V evaluate(final ILabeledPath<N, A> solutionPath) throws InterruptedException, PathEvaluationException {
		this.logger.info("Forwarding evaluation to plan evaluator {}", this.planEvaluator.getClass().getName());
		try {
			return this.planEvaluator.evaluate(this.baseReduction.decodeSolution(solutionPath));
		} catch (ObjectEvaluationFailedException e) {
			throw new PathEvaluationException("Forwarding exception", e);
		}
	}
}
