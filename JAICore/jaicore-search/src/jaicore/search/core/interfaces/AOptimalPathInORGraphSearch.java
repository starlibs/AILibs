package jaicore.search.core.interfaces;

import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AOptimizer;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.DelayedCancellationCheckException;
import jaicore.basic.algorithm.exceptions.DelayedTimeoutCheckException;
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
 * @param <NSrc>
 * @param <ASrc>
 * @param <V>
 * @param <NSearch>
 * @param <Asearch>
 */
public abstract class AOptimalPathInORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, NSrc, ASrc, V extends Comparable<V>, NSearch, Asearch> extends AOptimizer<I, EvaluatedSearchGraphPath<NSrc, ASrc, V>, V>
		implements IOptimalPathInORGraphSearch<I, NSrc, ASrc, V, NSearch, Asearch> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;

	public AOptimalPathInORGraphSearch(final I problem) {
		super(problem);
	}

	protected AOptimalPathInORGraphSearch( final IAlgorithmConfig config,final I problem) {
		super(config,problem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V> nextSolutionCandidateEvent() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		return (EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V>) super.nextSolutionCandidateEvent();
	}
	
	protected EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V> registerSolution(final EvaluatedSearchGraphPath<NSrc, ASrc, V> path) {
		updateBestSeenSolution(path);
		EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(path);
		this.post(event);
		return event;
	}

	@Override
	public GraphGenerator<NSrc, ASrc> getGraphGenerator() {
		return this.getInput().getGraphGenerator();
	}
	
	protected void checkAndConductTermination() throws TimeoutException, AlgorithmExecutionCanceledException, InterruptedException {
		try {
			super.checkAndConductTermination();
		} catch (DelayedTimeoutCheckException e) {
			e.printStackTrace();
			throw e.getException();
		} catch (DelayedCancellationCheckException e) {
			e.printStackTrace();
			throw e.getException();
		}
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
