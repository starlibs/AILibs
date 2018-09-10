package jaicore.search.algorithms.standard;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.RuntimeErrorException;

import com.google.common.eventbus.EventBus;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.core.interfaces.IGraphSearch;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GraphSearchInput;

public abstract class AbstractORGraphSearch<I extends GraphSearchInput<NSrc, ASrc>, O, NSrc, ASrc, V extends Comparable<V>, NSearch, Asearch>
		implements IGraphSearch<I, O, NSrc, ASrc, V, NSearch, Asearch> {

	private final EventBus eventBus = new EventBus();

	private boolean shutdownInitialized = false;
	private AlgorithmState state = AlgorithmState.created;
	private boolean interrupted;
	private boolean canceled;
	private boolean timeouted;
	private Timer timeouter;
	protected final I problem;
	private int timeoutInMS = Integer.MAX_VALUE;
	private Set<Thread> activeThreads = new HashSet<>();

	public AbstractORGraphSearch(I problem) {
		super();
		this.problem = problem;
	}

	@SuppressWarnings("unchecked")
	public <U extends SearchGraphPath<NSrc, ASrc>> U nextSolution() throws InterruptedException, AlgorithmExecutionCanceledException, NoSuchElementException {
		for (AlgorithmEvent event : this) {
			if (event instanceof GraphSearchSolutionCandidateFoundEvent)
				return (U) ((GraphSearchSolutionCandidateFoundEvent<NSrc, ASrc>) event).getSolutionCandidate();
		}
		throw new NoSuchElementException();
	}

	@Override
	public I getInput() {
		return problem;
	}

	@Override
	public GraphGenerator<NSrc, ASrc> getGraphGenerator() {
		return problem.getGraphGenerator();
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public boolean isTimeouted() {
		return timeouted;
	}

	public boolean isStopCriterionSatisfied() {
		return interrupted || canceled || timeouted;
	}

	protected void setInterrupted(boolean interrupted) {
		this.interrupted = interrupted;
	}

	protected void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	protected void setTimeouted(boolean timeouted) {
		this.timeouted = timeouted;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		if (timeUnit != TimeUnit.MILLISECONDS)
			throw new IllegalArgumentException("Only ms as time unit supported for the time being.");
		timeoutInMS = timeout;
	}

	@Override
	public int getTimeout() {
		return timeoutInMS;
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		return TimeUnit.MILLISECONDS;
	}

	protected void activateTimeoutTimer(String name) {
		timeouter = new Timer(name);
		timeouter.schedule(new TimerTask() {
			@Override
			public void run() {
				AbstractORGraphSearch.this.timeouted = true;
				shutdown();
			}
		}, timeoutInMS);
	}

	protected void checkTermination() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException {
		if (isTimeouted())
			throw new TimeoutException();
		if (isCanceled()) {
			throw new AlgorithmExecutionCanceledException(); // for a controlled cancel from outside on the algorithm
		}
		if (Thread.currentThread().isInterrupted() || isInterrupted()) {
			this.setInterrupted(true);
			throw new InterruptedException(); // if the thread itself was actively interrupted by somebody
		}
	}

	@Override
	public void cancel() {
		this.canceled = true;
		shutdown();
	}

	protected void shutdown() {
		synchronized (this) {
			if (shutdownInitialized) {
				return;
			}
			shutdownInitialized = true;
		}
		state = AlgorithmState.inactive;
		activeThreads.forEach(t -> t.interrupt());
		if (timeouter != null)
			timeouter.cancel();
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
			shutdown();
			throw new RuntimeException(e);
		}
	}

	public abstract AlgorithmEvent nextWithException() throws Exception;

	@Override
	public O call() throws Exception {
		try {
			while (hasNext()) {
				this.next();
			}
		} catch (RuntimeException e) {
			if (e.getCause() instanceof InterruptedException) {
				throw (InterruptedException) e.getCause();
			} else if (e.getCause() instanceof AlgorithmExecutionCanceledException) {
				throw (AlgorithmExecutionCanceledException) e.getCause();
			} else if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			} else
				throw new RuntimeException(e);
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
