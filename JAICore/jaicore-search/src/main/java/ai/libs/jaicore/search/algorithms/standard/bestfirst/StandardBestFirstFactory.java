package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import java.util.Objects;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

public class StandardBestFirstFactory<N, A, V extends Comparable<V>> extends BestFirstFactory<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> implements ILoggingCustomizable {

	private Logger logger = LoggerFactory.getLogger(StandardBestFirstFactory.class);

	public void setNodeEvaluator(final IPathEvaluator<N, A, V> nodeEvaluator) {
		GraphSearchWithSubpathEvaluationsInput<N, A, V> problem = this.getInput();
		IGraphGenerator<N, A> gg = problem != null ? problem.getGraphGenerator() : null;
		IPathGoalTester<N, A> gt = problem != null ? problem.getGoalTester() : null;
		this.setProblemInput(new GraphSearchWithSubpathEvaluationsInput<>(gg, gt, nodeEvaluator));
	}

	public void setGraphGenerator(final IGraphGenerator<N, A> graphGenerator) {
		GraphSearchWithSubpathEvaluationsInput<N, A, V> problem = this.getInput();
		Objects.requireNonNull(problem);
		IPathGoalTester<N, A> gt = problem.getGoalTester();
		IPathEvaluator<N, A, V> evaluator = problem.getPathEvaluator();
		this.setProblemInput(new GraphSearchWithSubpathEvaluationsInput<>(graphGenerator, gt, evaluator));
	}

	@Override
	public BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> getAlgorithm() {
		if (this.getInput().getGraphGenerator() == null) {
			throw new IllegalStateException("Cannot produce BestFirst searches before the graph generator is set in the problem.");
		}
		if (this.getInput().getPathEvaluator() == null) {
			throw new IllegalStateException("Cannot produce BestFirst searches before the node evaluator is set.");
		}

		/* determine search problem */
		GraphSearchWithSubpathEvaluationsInput<N, A, V> problem = this.getInput();
		this.logger.debug("Created algorithm input with\n\tgraph generator: {}\n\tnode evaluator: {}", problem.getGraphGenerator(), problem.getPathEvaluator());
		BestFirst<GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V> search = new BestFirst<>(problem);
		search.setTimeoutForComputationOfF(this.getTimeoutForFInMS(), this.getTimeoutEvaluator());
		if (this.getLoggerName() != null && this.getLoggerName().length() > 0) {
			search.setLoggerName(this.getLoggerName());
		}
		return search;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
