package ai.libs.jaicore.search.probleminputs;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;
import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.logging.ToJSONStringUtil;

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
public class GraphSearchWithPathEvaluationsInput<N, A, V extends Comparable<V>> extends GraphSearchInput<N, A> implements IGraphSearchWithPathEvaluationsInput<N, A, V> {
	private final IPathEvaluator<N, A, V> pathEvaluator;

	public GraphSearchWithPathEvaluationsInput(final IGraphSearchInput<N, A> graphSearchInput, final IPathEvaluator<N, A, V> pathEvaluator) {
		super(graphSearchInput);
		this.pathEvaluator = pathEvaluator;
	}

	public GraphSearchWithPathEvaluationsInput(final IGraphGenerator<N, A> graphGenerator, final PathGoalTester<N, A> goalTester, final IPathEvaluator<N, A, V> pathEvaluator) {
		super(graphGenerator, goalTester);
		this.pathEvaluator = pathEvaluator;
	}

	@Override
	public IPathEvaluator<N, A, V> getPathEvaluator() {
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
