package ai.libs.jaicore.search.probleminputs;

import java.util.HashMap;
import java.util.Map;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class GraphSearchWithPathEvaluationsInput<N, A, V extends Comparable<V>> extends GraphSearchInput<N, A> implements IPathSearchWithPathEvaluationsInput<N, A, V> {
	private final IPathEvaluator<N, A, V> pathEvaluator;

	public GraphSearchWithPathEvaluationsInput(final IPathSearchInput<N, A> graphSearchInput, final IPathEvaluator<N, A, V> pathEvaluator) {
		super(graphSearchInput);
		this.pathEvaluator = pathEvaluator;
	}

	@SuppressWarnings("unchecked")
	public GraphSearchWithPathEvaluationsInput(final IPathSearchInput<N, A> graphSearchInput, final IObjectEvaluator<ILabeledPath<N, A>, V> pathEvaluator) {
		this (graphSearchInput, pathEvaluator instanceof IPathEvaluator ? (IPathEvaluator<N, A, V>)pathEvaluator : new Evaluator<N, A, V>(pathEvaluator));
	}

	public GraphSearchWithPathEvaluationsInput(final IGraphGenerator<N, A> graphGenerator, final IPathGoalTester<N, A> goalTester, final IObjectEvaluator<ILabeledPath<N, A>, V> pathEvaluator) {
		this(new GraphSearchInput<>(graphGenerator, goalTester), pathEvaluator);
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

	private static class Evaluator<N, A, V extends Comparable<V>> implements IPathEvaluator<N, A, V>, ILoggingCustomizable {

		private Logger logger = LoggerFactory.getLogger(Evaluator.class);
		private final IObjectEvaluator<ILabeledPath<N, A>, V> pathEvaluator;

		public Evaluator(final IObjectEvaluator<ILabeledPath<N, A>, V> pathEvaluator) {
			super();
			if (pathEvaluator instanceof IPathEvaluator) {
				throw new IllegalArgumentException("An object of type " + IPathEvaluator.class.getName() + " should not be wrapped here!");
			}
			this.pathEvaluator = pathEvaluator;
		}

		@Override
		public V evaluate(final ILabeledPath<N, A> path) throws PathEvaluationException, InterruptedException {
			try {
				this.logger.info("Forwarding query for path of length {} to {}", path.getNumberOfNodes(), this.pathEvaluator.getClass().getName());
				return this.pathEvaluator.evaluate(path);
			} catch (ObjectEvaluationFailedException e) {
				throw new PathEvaluationException(e.getMessage(), e.getCause());
			}
		}

		@Override
		public String getLoggerName() {
			return this.logger.getName();
		}

		@Override
		public void setLoggerName(final String name) {
			this.logger = LoggerFactory.getLogger(name);
			if (this.pathEvaluator instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) this.pathEvaluator).setLoggerName(name + ".fw");
			}
		}
	}
}
