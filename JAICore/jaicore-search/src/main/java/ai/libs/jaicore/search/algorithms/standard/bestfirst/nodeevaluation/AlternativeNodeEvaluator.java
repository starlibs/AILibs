package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallyGraphDependentPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.logging.ToJSONStringUtil;

/**
 * This node evaluator can be used
 * a) if there is a prioritized node evaluator that should be used unless it returns NULL
 * b) to realize dead-end recognition
 * c) to use different node evaluators in different regions of the search graph
 *
 * @author fmohr
 *
 * @param <N>
 * @param <V>
 */
public class AlternativeNodeEvaluator<N, A, V extends Comparable<V>> extends DecoratingNodeEvaluator<N, A, V> implements ILoggingCustomizable {

	private String loggerName;
	private final IPathEvaluator<N, A, V> ne1;

	public AlternativeNodeEvaluator(final IPathEvaluator<N, A, V> ne1, final IPathEvaluator<N, A, V> ne2) {
		super(ne2);
		if (ne1 == null) {
			throw new IllegalArgumentException("The alternativ evaluator in node evaluator must not be null!");
		}
		this.ne1 = ne1;
	}

	@Override
	public boolean requiresGraphGenerator() {
		if (super.requiresGraphGenerator()) {
			return true;
		}
		return (this.ne1 instanceof IPotentiallyGraphDependentPathEvaluator) && ((IPotentiallyGraphDependentPathEvaluator<?, ?, ?>) this.ne1).requiresGraphGenerator();
	}

	public boolean doesPrimaryNodeEvaluatorReportSolutions() {
		return (this.ne1 instanceof IPotentiallySolutionReportingPathEvaluator) && ((IPotentiallySolutionReportingPathEvaluator<?, ?,  ?>) this.ne1).reportsSolutions();
	}

	public IPathEvaluator<N, A, V> getPrimaryNodeEvaluator() {
		return this.ne1;
	}

	@Override
	public boolean reportsSolutions() {
		if (super.reportsSolutions()) {
			return true;
		}
		return this.doesDecoratedEvaluatorReportSolutions();
	}

	@Override
	public void setGenerator(final IGraphGenerator<N, A> generator, final IPathGoalTester<N, A> goalTester) {
		super.setGenerator(generator, goalTester);
		if (!(this.ne1 instanceof IPotentiallyGraphDependentPathEvaluator)) {
			return;
		}
		IPotentiallyGraphDependentPathEvaluator<N, A, V> castedNE1 = (IPotentiallyGraphDependentPathEvaluator<N, A, V>) this.ne1;
		if (castedNE1.requiresGraphGenerator()) {
			castedNE1.setGenerator(generator, goalTester);
		}
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		if (super.doesDecoratedEvaluatorReportSolutions()) {
			super.registerSolutionListener(listener);
		}
		if (this.doesPrimaryNodeEvaluatorReportSolutions()) {
			((IPotentiallySolutionReportingPathEvaluator<?, ?,  ?>) this.ne1).registerSolutionListener(listener);
		}
	}

	@Override
	public V evaluate(final ILabeledPath<N, A> node) throws PathEvaluationException, InterruptedException {
		V f1 = this.ne1.evaluate(node);
		if (f1 != null) {
			return f1;
		}
		return super.evaluate(node);
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("primary", this.ne1);
		fields.put("secondary", super.getEvaluator());
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.loggerName = name;
		super.setLoggerName(name + "._decorating");
		if (this.ne1 instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.ne1).setLoggerName(name + ".primary");
		}
		if (this.getEvaluator() instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.getEvaluator()).setLoggerName(name + ".secondary");
		}
	}
}
