package ai.libs.jaicore.search.probleminputs;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.graphsearch.problem.IGraphSearchInput;
import org.api4.java.ai.graphsearch.problem.IGraphSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.PathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.datastructure.graph.IPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.RootGenerator;
import org.api4.java.datastructure.graph.implicit.SuccessorGenerator;

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

	public GraphSearchWithPathEvaluationsInput(final IGraphSearchInput<N, A> graphSearchInput, final IObjectEvaluator<IPath<N, A>, V> pathEvaluator) {
		this (graphSearchInput, new IPathEvaluator<N, A, V>() {

			@Override
			public V evaluate(final IPath<N, A> path) throws PathEvaluationException, InterruptedException {
				try {
					return pathEvaluator.evaluate(path);
				} catch (ObjectEvaluationFailedException e) {
					throw new PathEvaluationException(e.getMessage(), e.getCause());
				}
			}
		});
	}

	public GraphSearchWithPathEvaluationsInput(final IGraphGenerator<N, A> graphGenerator, final PathGoalTester<N, A> goalTester, final IObjectEvaluator<IPath<N, A>, V> pathEvaluator) {
		this(new GraphSearchInput<>(graphGenerator, goalTester), pathEvaluator);
	}

	/**
	 * Clones a problem but uses a different start node instead.
	 *
	 * @param originalProblem
	 * @param alternativeRootGenerator
	 */
	public GraphSearchWithPathEvaluationsInput(final GraphSearchWithPathEvaluationsInput<N, A, V> originalProblem, final RootGenerator<N> alternativeRootGenerator) {
		this (new IGraphGenerator<N, A>() {

			@Override
			public RootGenerator<N> getRootGenerator() {
				return alternativeRootGenerator;
			}

			@Override
			public SuccessorGenerator<N, A> getSuccessorGenerator() {
				return originalProblem.getGraphGenerator().getSuccessorGenerator();
			}
		}, originalProblem.getGoalTester(), originalProblem.getPathEvaluator());
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
