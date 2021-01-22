package ai.libs.jaicore.search.algorithms.standard.rrm;

import java.util.ArrayList;
import java.util.List;

import org.api4.java.ai.graphsearch.problem.IPathInORGraphSearch;
import org.api4.java.ai.graphsearch.problem.IPathSearchInput;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;
import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;

/**
 * @author Felix Mohr
 *
 *         This search combines several search algorithms and steps them in a round-robin fashion.
 */
public class RoundRobinMetaSearch<I extends IPathSearchInput<N, A>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> implements ILoggingCustomizable {

	private int currentIndex = 0;
	private List<IPathInORGraphSearch<?, ?, N, A>> portfolio;
	private List<IPathInORGraphSearch<?, ?, N, A>> activePortfolio;

	public RoundRobinMetaSearch(final IOwnerBasedAlgorithmConfig config, final I problem, final List<IPathInORGraphSearch<?, ?, N, A>> portfolio) {
		super(config, problem);
		this.portfolio = portfolio;
		this.activePortfolio = new ArrayList<>(portfolio);
		portfolio.forEach(a -> a.registerListener(this));
	}

	public RoundRobinMetaSearch(final I problem, final List<IPathInORGraphSearch<?, ?, N, A>> portfolio) {
		this(null, problem, portfolio);
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:
			for (IPathInORGraphSearch<?, ?, N, A> a : this.portfolio) {
				a.nextWithException();
			}
			return this.activate();
		case ACTIVE:
			this.checkAndConductTermination();
			int n = this.activePortfolio.size();
			while (!this.activePortfolio.get(this.currentIndex % n).hasNext()) {
				this.activePortfolio.remove(this.currentIndex % n);
				n --;
			}
			if (this.activePortfolio.isEmpty()) {
				return this.terminate();
			}
			else {
				IPathInORGraphSearch<?, ?, N, A> algorithm = this.activePortfolio.get(this.currentIndex % n);
				IAlgorithmEvent event = algorithm.nextWithException();
				this.currentIndex++;
				return event instanceof AlgorithmFinishedEvent ? this.nextWithException() : event; // if the portfolio algorithm has finished, just return the next one
			}
		default:
			throw new IllegalStateException("Cannot do anything anymore");
		}

	}

	@Subscribe
	public void receiveEvent(final IAlgorithmEvent e) {
		if (!(e instanceof AlgorithmInitializedEvent || e instanceof AlgorithmFinishedEvent)) {
			this.post(e);
		}
	}
}
