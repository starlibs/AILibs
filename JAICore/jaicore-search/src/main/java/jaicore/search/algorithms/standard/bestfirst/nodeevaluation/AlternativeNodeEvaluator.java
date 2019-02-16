package jaicore.search.algorithms.standard.bestfirst.nodeevaluation;

import java.util.HashMap;
import java.util.Map;

import jaicore.basic.ILoggingCustomizable;
import jaicore.logging.ToJSONStringUtil;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.model.travesaltree.Node;

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
		if (ne1 == null)
			throw new IllegalArgumentException("The alternativ evaluator in node evaluator must not be null!");
		this.ne1 = ne1;
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
		return loggerName;
	}

	@Override
	public void setLoggerName(String name) {
		this.loggerName = name;
		if (this.ne1 instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.ne1).setLoggerName(name + ".primary");
		}
		if (this.getEvaluator() instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) this.getEvaluator()).setLoggerName(name + ".secondary");
		}
	}
}
