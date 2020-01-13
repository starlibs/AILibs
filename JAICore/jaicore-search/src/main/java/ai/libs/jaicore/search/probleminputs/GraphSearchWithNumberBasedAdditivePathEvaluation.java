package ai.libs.jaicore.search.probleminputs;

import java.util.Iterator;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;

import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class GraphSearchWithNumberBasedAdditivePathEvaluation<N, A> extends GraphSearchWithSubpathEvaluationsInput<N, A, Double> {

	public interface EdgeCostComputer<N, A> {
		public double g(BackPointerPath<N, A, ?> from, BackPointerPath<N, A, ?> to);
	}

	public static class FComputer<N, A> implements IPathEvaluator<N, A, Double> {

		private final EdgeCostComputer<N, A> g;
		private final IPathEvaluator<N, A, Double> h; // this is to estimate the minimum cost between a node and ANY goal node

		public FComputer(final EdgeCostComputer<N, A> g, final IPathEvaluator<N, A, Double> h) {
			super();
			this.g = g;
			this.h = h;
		}

		@Override
		public Double evaluate(final ILabeledPath<N, A> path) throws PathEvaluationException, InterruptedException {
			if (!(path instanceof BackPointerPath)) {
				throw new IllegalArgumentException("Can compute f-value only for back pointer paths.");
			}
			List<?> cPath = ((BackPointerPath<N, A, Double>)path).path();
			int depth = cPath.size() - 1;
			double pathCost = 0;
			double heuristic = this.h.evaluate(path);
			if (depth > 0) {
				@SuppressWarnings("unchecked")
				Iterator<BackPointerPath<N, A, ?>> it = (Iterator<BackPointerPath<N, A, ?>>) cPath.iterator();
				BackPointerPath<N, A, ?> parent = it.next();
				BackPointerPath<N, A, ?> current;
				while (it.hasNext()) {
					current = it.next();
					pathCost += this.g.g(parent, current);
					parent = current;
				}
			}
			return pathCost + heuristic;
		}

		public EdgeCostComputer<N, A> getG() {
			return this.g;
		}

		public IPathEvaluator<N, A, Double> getH() {
			return this.h;
		}
	}

	public GraphSearchWithNumberBasedAdditivePathEvaluation(final IPathSearchInput<N, A> baseProblem, final EdgeCostComputer<N, A> g, final IPathEvaluator<N, A, Double> h) {
		this(baseProblem.getGraphGenerator(), baseProblem.getGoalTester(), new FComputer<>(g, h));
	}

	public GraphSearchWithNumberBasedAdditivePathEvaluation(final IGraphGenerator<N, A> graphGenerator, final IPathGoalTester<N, A> goalTester, final EdgeCostComputer<N, A> g, final IPathEvaluator<N, A, Double> h) {
		this(graphGenerator, goalTester, new FComputer<>(g, h));
	}

	public GraphSearchWithNumberBasedAdditivePathEvaluation(final IPathSearchInput<N, A> baseProblem, final FComputer<N, A> fComputer) {
		this(baseProblem.getGraphGenerator(), baseProblem.getGoalTester(), fComputer);
	}

	/**
	 * This constructor can be used if one wants to extend AStar by some more specific f-value computer.
	 * See R* for an example.
	 *
	 * @param graphGenerator
	 * @param fComputer
	 */
	public GraphSearchWithNumberBasedAdditivePathEvaluation(final IGraphGenerator<N, A> graphGenerator, final IPathGoalTester<N, A> goalTester, final FComputer<N, A> fComputer) {
		super(graphGenerator, goalTester, fComputer);
	}

}
