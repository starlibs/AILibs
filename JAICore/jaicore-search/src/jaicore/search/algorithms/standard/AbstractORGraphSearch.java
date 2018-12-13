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

	private boolean shutdownInitialized = false;
	private boolean timeouted;
	private Timer timeouter;
	private final Set<Thread> activeThreads = new HashSet<>();
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

	public boolean isTimeouted() {
		return this.timeouted;
	}

	public boolean isStopCriterionSatisfied() {
		return this.isCanceled() || this.timeouted;
	}

	protected void setTimeouted(final boolean timeouted) {
		this.timeouted = timeouted;
	}

	protected void activateTimeoutTimer(final String name) {
		if (this.getTimeout() == null) {
			return;
		}
		this.timeouter = new Timer(name);
		this.timeouter.schedule(new TimerTask() {
			@Override
			public void run() {
				AbstractORGraphSearch.this.timeouted = true;
				AbstractORGraphSearch.this.logger.info("Timeout triggered. Have set the timeouted flag to true and will now invoke shutdown procedure.");
				AbstractORGraphSearch.this.shutdown();
			}
		}, this.getTimeout().milliseconds());
		this.logger.info("Timeouter {} activated for in {}ms", name, this.getTimeout().milliseconds());
	}

	protected void checkTermination() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		this.logger.debug("Checking Termination of {}", this);
		if (this.isTimeouted()) {
			this.logger.info("Timeout detected for {}, stopping execution with TimeoutException", this);
			throw new TimeoutException();
		}
		if (this.isCanceled()) {
			this.logger.info("Cancel detected for {}, stopping execution with AlgorithmExceptionCanceledException", this);
			throw new AlgorithmExecutionCanceledException(); // for a controlled cancel from outside on the algorithm
		}
		if (Thread.currentThread().isInterrupted()) {
			this.logger.info("Interruption detected for {}, stopping execution with InterruptedException", this);
			throw new InterruptedException(); // if the thread itself was actively interrupted by somebody
		}
	}

	protected AlgorithmInitializedEvent activate() {
		this.switchState(AlgorithmState.active);
		AlgorithmInitializedEvent event = new AlgorithmInitializedEvent();
		this.post(event);
		return event;
	}

	@Override
	public void cancel() {
		super.cancel();
		this.logger.info("Executing cancel on {}. Have set the cancel flag and will now invoke shutdown procedure.", this);
		this.shutdown();
	}

	protected void unregisterThreadAndShutdown() {
		this.unregisterActiveThread();
		this.shutdown();
	}

	protected void shutdown() {
		synchronized (this) {
			if (this.shutdownInitialized) {
				this.logger.info("Tried to enter shudtown for {}, but the shutdown has already been initialized in the past, so exiting the shutdown block.", this);
				return;
			}
			this.shutdownInitialized = true;
		}
		this.logger.info("Entering shutdown procedure for {}. Setting algorithm state from {} to inactive and interrupting potentially active threads.", this, this.getState());
		this.setState(AlgorithmState.inactive);
		this.activeThreads.forEach(t -> {
			this.logger.info("Interrupting {} on behalf of shutdown of {}", t, this);
			t.interrupt();
		});
		if (this.timeouter != null) {
			this.logger.info("Canceling timeouter {}", this.timeouter);
			this.timeouter.cancel();
		}
		this.logger.info("Shutdown of {} completed.", this);
	}

	protected void registerActiveThread() {
		this.activeThreads.add(Thread.currentThread());
	}

	protected void unregisterActiveThread() {
		this.activeThreads.remove(Thread.currentThread());
	}

	@Override
	public boolean hasNext() {
		return this.getState() != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return this.nextWithException();
		} catch (Exception e) {
			this.setState(AlgorithmState.inactive);
			this.unregisterThreadAndShutdown();
			if (e instanceof InterruptedException && this.timeouted) {
				return new AlgorithmFinishedEvent();
			}
			throw new RuntimeException(e);
		}
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

	public boolean isShutdownInitialized() {
		return this.shutdownInitialized;
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
