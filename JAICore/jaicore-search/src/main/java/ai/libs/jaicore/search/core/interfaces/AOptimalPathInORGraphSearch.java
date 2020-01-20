package ai.libs.jaicore.search.core.interfaces;

import org.api4.java.ai.graphsearch.problem.IOptimalPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AAlgorithm;
import ai.libs.jaicore.basic.algorithm.AOptimizer;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;

/**
 * This is a template for algorithms that aim at finding paths from a root to
 * goal nodes in a graph. This template does not assume paths to have a score.
 *
 * The output type of this algorithm is fixed to EvaluatedSearchGraphPath<NSrc, ASrc, V>
 *
 * @author fmohr
 *
 * @param <I>
 * @param <N>
 * @param <A>
 * @param <V>
 * @param <NSearch>
 * @param <Asearch>
 */
public abstract class AOptimalPathInORGraphSearch<I extends IPathSearchInput<N, A>, N, A, V extends Comparable<V>> extends AOptimizer<I, EvaluatedSearchGraphPath<N, A, V>, V>
implements IOptimalPathInORGraphSearch<I, EvaluatedSearchGraphPath<N, A, V>, N, A, V> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	public AOptimalPathInORGraphSearch(final I problem) {
		super(problem);
	}

	protected AOptimalPathInORGraphSearch(final IOwnerBasedAlgorithmConfig config,final I problem) {
		super(config,problem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> nextSolutionCandidateEvent() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		return (EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>) super.nextSolutionCandidateEvent();
	}

	protected EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> registerSolution(final EvaluatedSearchGraphPath<N, A, V> path) {
		this.updateBestSeenSolution(path);
		EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(this, path);
		this.logger.info("Identified solution with score {}. Enable DEBUG to see the concrete nodes and actions.", path.getScore());
		this.logger.debug("Nodes: {}. Actions: {}", path.getNodes(), path.getArcs());
		this.post(event);
		return event;
	}

	@Override
	public IGraphGenerator<N, A> getGraphGenerator() {
		return this.getInput().getGraphGenerator();
	}

	public IPathGoalTester<N, A> getGoalTester() {
		return this.getInput().getGoalTester();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger to {}", name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched to logger {}", name);
		super.setLoggerName(this.loggerName + "._algorithm");
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}
