package jaicore.search.core.interfaces;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.ASolutionCandidateIterator;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.DelayedCancellationCheckException;
import jaicore.basic.algorithm.exceptions.DelayedTimeoutCheckException;
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
 * @param <NSrc>
 * @param <ASrc>
 * @param <V>
 * @param <NSearch>
 * @param <Asearch>
 */
public abstract class AAnyPathInORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, O extends SearchGraphPath<NSrc, ASrc>, NSrc, ASrc, NSearch, Asearch> extends ASolutionCandidateIterator<I, O>
		implements IPathInORGraphSearch<I, O, NSrc, ASrc, NSearch, Asearch> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	public AAnyPathInORGraphSearch(final I problem) {
		super(problem);
	}

	protected AAnyPathInORGraphSearch(final I problem, final IAlgorithmConfig config) {
		super(problem, config);
	}
	
	protected GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc, O> registerSolution(final O path) {
		GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc, O> event = new GraphSearchSolutionCandidateFoundEvent<>(path);
		this.post(event);
		return event;
	}

	@Override
	public GraphGenerator<NSrc, ASrc> getGraphGenerator() {
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
	
	protected void checkTermination() throws TimeoutException, AlgorithmExecutionCanceledException, InterruptedException {
		try {
			super.checkTermination();
		} catch (DelayedTimeoutCheckException e) {
			logger.warn("CheckTermination was called with delay. Message: \"{}\"", e.getMessage());
			throw e.getException();
		} catch (DelayedCancellationCheckException e) {
			logger.warn("CheckTermination was called with delay. Message: \"{}\"", e.getMessage());
			throw e.getException();
		}
	}
}
