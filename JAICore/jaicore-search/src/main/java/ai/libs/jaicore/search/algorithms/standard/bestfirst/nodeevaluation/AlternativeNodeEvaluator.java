package ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.common.control.ILoggingCustomizable;

import ai.libs.jaicore.logging.ToJSONStringUtil;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import ai.libs.jaicore.search.core.interfaces.GraphGenerator;
import ai.libs.jaicore.search.model.travesaltree.Node;

/**
 * This node evaluator can be used
 * 	a) if there is a prioritized node evaluator that should be used unless it returns NULL
 *  b) to realize dead-end recognition
 *  c) to use different node evaluators in different regions of the search graph
 *
 * @author fmohr
 *
 * @param <T>
 * @param <V>
 */
public class AlternativeNodeEvaluator<T, V extends Comparable<V>> extends DecoratingNodeEvaluator<T, V> implements ILoggingCustomizable {

	private String loggerName;
	private final INodeEvaluator<T, V> ne1;

	public AlternativeNodeEvaluator(final INodeEvaluator<T, V> ne1, final INodeEvaluator<T, V> ne2) {
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
		return (this.ne1 instanceof IPotentiallyGraphDependentNodeEvaluator) && ((IPotentiallyGraphDependentNodeEvaluator<?, ?>)this.ne1).requiresGraphGenerator();
	}

	public boolean doesPrimaryNodeEvaluatorReportSolutions() {
		return (this.ne1 instanceof IPotentiallySolutionReportingNodeEvaluator) && ((IPotentiallySolutionReportingNodeEvaluator<?, ?>)this.ne1).reportsSolutions();
	}

	@Override
	public boolean reportsSolutions() {
		if (super.reportsSolutions()) {
			return true;
		}
		return this.doesDecoratedEvaluatorReportSolutions();
	}

	@Override
	public void setGenerator(final GraphGenerator<T, ?> generator) {
		super.setGenerator(generator);
		if (!(this.ne1 instanceof IPotentiallyGraphDependentNodeEvaluator)) {
			return;
		}
		IPotentiallyGraphDependentNodeEvaluator<T, V> castedNE1 = (IPotentiallyGraphDependentNodeEvaluator<T, V>)this.ne1;
		if (castedNE1.requiresGraphGenerator()) {
			castedNE1.setGenerator(generator);
		}
	}

	@Override
	public void registerSolutionListener(final Object listener) {
		if (super.doesDecoratedEvaluatorReportSolutions()) {
			super.registerSolutionListener(listener);
		}
		if (this.doesPrimaryNodeEvaluatorReportSolutions()) {
			((IPotentiallySolutionReportingNodeEvaluator<?, ?>)this.ne1).registerSolutionListener(listener);
		}
	}

	@Override
	public V f(final Node<T, ?> node) throws NodeEvaluationException, InterruptedException {
		V f1 = this.ne1.f(node);
		if (f1 != null) {
			return f1;
		}
		return super.f(node);
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
