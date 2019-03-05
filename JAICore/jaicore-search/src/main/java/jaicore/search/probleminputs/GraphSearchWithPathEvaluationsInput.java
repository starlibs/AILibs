package jaicore.search.probleminputs;

import java.util.HashMap;
import java.util.Map;

import jaicore.logging.ToJSONStringUtil;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.ISolutionEvaluator;

/**
 * In AILibs, a graph search problem always aims at identifying one or more paths from
 * a set of root nodes to a goal node. Usually, such paths are associated with a value
 * that qualifies them.
 *
 * This is the most general problem input one can have if there is no other knowledge.
 *
 * @author fmohr
 *
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class GraphSearchWithPathEvaluationsInput<N, A, V extends Comparable<V>> extends GraphSearchInput<N, A> {
	private final ISolutionEvaluator<N, A, V> pathEvaluator;

	public GraphSearchWithPathEvaluationsInput(final GraphGenerator<N, A> graphGenerator, final ISolutionEvaluator<N, A, V> pathEvaluator) {
		super(graphGenerator);
		this.pathEvaluator = pathEvaluator;
	}

	public ISolutionEvaluator<N, A, V> getPathEvaluator() {
		return this.pathEvaluator;
	}

	@Override
	public String toString() {
		Map<String, Object> fields = new HashMap<>();
		fields.put("pathEvaluator", this.pathEvaluator);
		fields.put("graphGenerator", super.getGraphGenerator());
		return ToJSONStringUtil.toJSONString(this.getClass().getSimpleName(), fields);
	}
}
