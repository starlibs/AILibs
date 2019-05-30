package jaicore.search.core.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AOptimizer;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.probleminputs.GraphSearchInput;

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
public abstract class AOptimalPathInORGraphSearch<I extends GraphSearchInput<N, A>, N, A, V extends Comparable<V>> extends AOptimizer<I, EvaluatedSearchGraphPath<N, A, V>, V>
implements IOptimalPathInORGraphSearch<I, N, A, V> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	public AOptimalPathInORGraphSearch(final I problem) {
		super(problem);
	}

	protected AOptimalPathInORGraphSearch(final IAlgorithmConfig config,final I problem) {
		super(config,problem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> nextSolutionCandidateEvent() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		return (EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>) super.nextSolutionCandidateEvent();
	}

	protected EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> registerSolution(final EvaluatedSearchGraphPath<N, A, V> path) {
		this.updateBestSeenSolution(path);
		EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(this.getId(), path);
		this.logger.info("Identified solution with score {}. Enable DEBUG to see the concrete nodes and actions.", path.getScore());
		this.logger.debug("Nodes: {}. Actions: {}", path.getNodes(), path.getEdges());
		this.post(event);
		return event;
	}

	@Override
	public GraphGenerator<N, A> getGraphGenerator() {
		return this.getInput().getGraphGenerator();
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
