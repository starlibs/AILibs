package jaicore.search.algorithms.standard;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.algorithm.AAlgorithm;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.basic.algorithm.IAlgorithmConfig;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchInput;

public abstract class AbstractORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, V extends Comparable<V>, NSearch, Asearch> extends AAlgorithm<I, O> implements IGraphSearch<I, O, NSrc, ASrc, V, NSearch, Asearch> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AAlgorithm.class);
	private String loggerName;
	
	private EvaluatedSearchGraphPath<NSrc, ASrc, V> bestSeenSolution;

	public AbstractORGraphSearch(final I problem) {
		super(problem);
	}

	protected AbstractORGraphSearch(final IAlgorithmConfig config, final I problem) {
		super(config, problem);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U extends SearchGraphPath<NSrc, ASrc>> U nextSolution() throws InterruptedException, AlgorithmExecutionCanceledException, NoSuchElementException {
		for (AlgorithmEvent event : this) {
			if (event instanceof GraphSearchSolutionCandidateFoundEvent) {
				if (event instanceof EvaluatedSearchSolutionCandidateFoundEvent) {
					EvaluatedSearchGraphPath<NSrc, ASrc, V> solutionCandidate = ((EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V>) event).getSolutionCandidate();
					if (this.bestSeenSolution == null || solutionCandidate.getScore().compareTo(this.bestSeenSolution.getScore()) < 0) {
						this.bestSeenSolution = solutionCandidate;
					}
				}
				return (U) ((GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc>) event).getSolutionCandidate();
			}
		}
		throw new NoSuchElementException();
	}

	protected GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc> registerSolution(final SearchGraphPath<NSrc, ASrc> path) {
		GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc> event = new GraphSearchSolutionCandidateFoundEvent<>(path);
		this.post(event);
		return event;
	}

	protected EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V> registerSolution(final EvaluatedSearchGraphPath<NSrc, ASrc, V> path) {
		if (this.bestSeenSolution == null || path.getScore().compareTo(this.bestSeenSolution.getScore()) < 0) {
			this.bestSeenSolution = path;
		}
		EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(path);
		this.post(event);
		return event;
	}

	@Override
	public EvaluatedSearchGraphPath<NSrc, ASrc, V> getBestSeenSolution() {
		return this.bestSeenSolution;
	}

	@Override
	public GraphGenerator<NSrc, ASrc> getGraphGenerator() {
		return this.getInput().getGraphGenerator();
	}
	
	@Override
	public O call() throws Exception {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.getSolutionProvidedToCall();
	}

	protected void switchState(final AlgorithmState state) {
		if (state == AlgorithmState.inactive) {
			throw new IllegalArgumentException("Cannot switch state to inactive. Use shutdown instead, which will set the state to inactive.");
		}
		this.setState(state);
	}

	public abstract O getSolutionProvidedToCall();

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
