package jaicore.search.algorithms.standard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import jaicore.basic.TimeOut;
import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchInput;

public abstract class AbstractORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, V extends Comparable<V>, NSearch, Asearch>
		implements IGraphSearch<I, O, NSrc, ASrc, V, NSearch, Asearch> {

	private Logger logger = LoggerFactory.getLogger(AbstractORGraphSearch.class);
	private final EventBus eventBus = new EventBus();

	private boolean shutdownInitialized = false;
	private AlgorithmState state = AlgorithmState.created;
	private boolean canceled;
	private boolean timeouted;
	private Timer timeouter;
	protected final I problem;
	private TimeOut timeout = null;
	private Set<Thread> activeThreads = new HashSet<>();
	private EvaluatedSearchGraphPath<NSrc, ASrc, V> bestSeenSolution;

	public AbstractORGraphSearch(I problem) {
		super();
		this.problem = problem;
	}

	@SuppressWarnings("unchecked")
	public <U extends SearchGraphPath<NSrc, ASrc>> U nextSolution() throws InterruptedException, AlgorithmExecutionCanceledException, NoSuchElementException {
		for (AlgorithmEvent event : this) {
			if (event instanceof GraphSearchSolutionCandidateFoundEvent) {
				if (event instanceof EvaluatedSearchSolutionCandidateFoundEvent) {
					EvaluatedSearchGraphPath<NSrc, ASrc, V> solutionCandidate = (EvaluatedSearchGraphPath<NSrc, ASrc, V>) ((EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V>) event)
							.getSolutionCandidate();
					if (bestSeenSolution == null || solutionCandidate.getScore().compareTo(bestSeenSolution.getScore()) < 0)
						bestSeenSolution = solutionCandidate;
				}
				return (U) ((GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc>) event).getSolutionCandidate();
			}
		}
		throw new NoSuchElementException();
	}

	protected GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc> registerSolution(SearchGraphPath<NSrc, ASrc> path) {
		GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc> event = new GraphSearchSolutionCandidateFoundEvent<>(path);
		eventBus.post(event);
		return event;
	}

	protected EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V> registerSolution(EvaluatedSearchGraphPath<NSrc, ASrc, V> path) {
		if (bestSeenSolution == null || path.getScore().compareTo(bestSeenSolution.getScore()) < 0)
			bestSeenSolution = path;
		EvaluatedSearchSolutionCandidateFoundEvent<NSrc, ASrc, V> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(path);
		eventBus.post(event);
		return event;
	}

	public EvaluatedSearchGraphPath<NSrc, ASrc, V> getBestSeenSolution() {
		return bestSeenSolution;
	}

	@Override
	public I getInput() {
		return problem;
	}

	@Override
	public GraphGenerator<NSrc, ASrc> getGraphGenerator() {
		return problem.getGraphGenerator();
	}

	public boolean isCanceled() {
		return canceled;
	}

	public boolean isTimeouted() {
		return timeouted;
	}

	public boolean isStopCriterionSatisfied() {
		return canceled || timeouted;
	}

	protected void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	protected void setTimeouted(boolean timeouted) {
		this.timeouted = timeouted;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		setTimeout(new TimeOut(timeout, timeUnit));
	}
	
	@Override
	public void setTimeout(TimeOut timeout) {
		this.timeout = timeout;
	}

	@Override
	public TimeOut getTimeout() {
		return timeout;
	}

	protected void activateTimeoutTimer(String name) {
		if (timeout == null)
			return;
		timeouter = new Timer(name);
		timeouter.schedule(new TimerTask() {
			@Override
			public void run() {
				AbstractORGraphSearch.this.timeouted = true;
				logger.info("Timeout triggered. Have set the timeouted flag to true and will now invoke shutdown procedure.");
				shutdown();
			}
		}, timeout.milliseconds());
		logger.info("Timeouter {} activated for in {}ms", name, timeout);
	}

	protected void checkTermination() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		logger.debug("Checking Termination of {}", this);
		if (isTimeouted()) {
			logger.info("Timeout detected for {}, stopping execution with TimeoutException", this);
			throw new TimeoutException();
		}
		if (isCanceled()) {
			logger.info("Cancel detected for {}, stopping execution with AlgorithmExceptionCanceledException", this);
			throw new AlgorithmExecutionCanceledException(); // for a controlled cancel from outside on the algorithm
		}
		if (Thread.currentThread().isInterrupted()) {
			logger.info("Interruption detected for {}, stopping execution with InterruptedException", this);
			throw new InterruptedException(); // if the thread itself was actively interrupted by somebody
		}
	}

	protected AlgorithmInitializedEvent activate() {
		switchState(AlgorithmState.active);
		AlgorithmInitializedEvent event = new AlgorithmInitializedEvent();
		postEvent(event);
		return event;
	}

	@Override
	public void cancel() {
		this.canceled = true;
		logger.info("Executing cancel on {}. Have set the cancel flag and will now invoke shutdown procedure.", this);
		shutdown();
	}

	protected void unregisterThreadAndShutdown() {
		unregisterActiveThread();
		shutdown();
	}

	protected void shutdown() {
		synchronized (this) {
			if (shutdownInitialized) {
				logger.info("Tried to enter shudtown for {}, but the shutdown has already been initialized in the past, so exiting the shutdown block.", this);
				return;
			}
			shutdownInitialized = true;
		}
		logger.info("Entering shutdown procedure for {}. Setting algorithm state from {} to inactive and interrupting potentially active threads.", this, state);
		state = AlgorithmState.inactive;
		activeThreads.forEach(t -> {
			logger.info("Interrupting {} on behalf of shutdown of {}", t, this);
			t.interrupt();
		});
		if (timeouter != null) {
			logger.info("Canceling timeouter {}", timeouter);
			timeouter.cancel();
		}
		logger.info("Shutdown of {} completed.", this);
	}

	protected void registerActiveThread() {
		activeThreads.add(Thread.currentThread());
	}

	protected void unregisterActiveThread() {
		activeThreads.remove(Thread.currentThread());
	}

	protected void postEvent(Object event) {
		eventBus.post(event);
	}

	@Override
	public void registerListener(Object listener) {
		eventBus.register(listener);
	}

	@Override
	public Iterator<AlgorithmEvent> iterator() {
		return this;
	}

	@Override
	public boolean hasNext() {
		return this.state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			return nextWithException();
		} catch (Exception e) {
			this.state = AlgorithmState.inactive;
			unregisterThreadAndShutdown();
			if (e instanceof InterruptedException && timeouted) {
				return new AlgorithmFinishedEvent();
			}
			throw new RuntimeException(e);
		}
	}

	public abstract AlgorithmEvent nextWithException() throws Exception;

	@Override
	public O call() throws Exception {
		while (hasNext()) {
			this.nextWithException();
		}
		return getSolutionProvidedToCall();
	}

	public AlgorithmState getState() {
		return state;
	}

	protected void switchState(AlgorithmState state) {
		if (state == AlgorithmState.inactive)
			throw new IllegalArgumentException("Cannot switch state to inactive. Use shutdown instead, which will set the state to inactive.");
		this.state = state;
	}

	public abstract O getSolutionProvidedToCall();

	public boolean isShutdownInitialized() {
		return shutdownInitialized;
	}
}
