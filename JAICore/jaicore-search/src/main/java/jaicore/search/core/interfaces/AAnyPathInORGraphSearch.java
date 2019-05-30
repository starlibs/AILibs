package jaicore.search.core.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.ASolutionCandidateIterator;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.model.other.SearchGraphPath;
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
public abstract class AAnyPathInORGraphSearch<I extends GraphSearchInput<N, A>, O extends SearchGraphPath<N, A>, N, A> extends ASolutionCandidateIterator<I, O>
		implements IPathInORGraphSearch<I, O, N, A> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	public AAnyPathInORGraphSearch(final I problem) {
		super(problem);
	}

	protected AAnyPathInORGraphSearch(final IAlgorithmConfig config,final I problem) {
		super(config,problem);
	}
	
	protected GraphSearchSolutionCandidateFoundEvent<N, A, O> registerSolution(final O path) {
		GraphSearchSolutionCandidateFoundEvent<N, A, O> event = new GraphSearchSolutionCandidateFoundEvent<>(getId(), path);
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
