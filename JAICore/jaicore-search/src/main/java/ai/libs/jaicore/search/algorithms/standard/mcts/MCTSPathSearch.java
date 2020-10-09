package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.api4.java.ai.graphsearch.problem.IPathSearchWithPathEvaluationsInput;
import org.api4.java.algorithm.Timeout;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.event.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.AlgorithmFinishedEvent;
import ai.libs.jaicore.basic.algorithm.AlgorithmInitializedEvent;
import ai.libs.jaicore.basic.algorithm.EAlgorithmState;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.search.algorithms.mdp.mcts.GraphBasedMDP;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTS;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSFactory;
import ai.libs.jaicore.search.algorithms.mdp.mcts.MCTSIterationCompletedEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.probleminputs.IMDP;

/**
 *
 * @author Felix Mohr
 *
 * @param <I> Problem type
 * @param <N> Type of states (nodes)
 * @param <A> Type of actions
 */
public class MCTSPathSearch<I extends IPathSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends AOptimalPathInORGraphSearch<I, N, A, Double> {

	private Logger logger = LoggerFactory.getLogger(MCTSPathSearch.class);
	private final GraphBasedMDP<N, A> mdp;
	private final MCTS<N, A> mcts;
	private final Set<Integer> hashCodesOfReturnedPaths = new HashSet<>();

	public MCTSPathSearch(final I problem, final MCTSFactory<N, A, ?> mctsFactory) {
		super(problem);
		this.mdp = new GraphBasedMDP<>(problem);
		this.mcts = mctsFactory.getAlgorithm(this.mdp);
		this.mcts.registerListener(new Object() {

			@Subscribe
			public void receiveMCTSEvent(final IAlgorithmEvent e) {
				if (!(e instanceof AlgorithmInitializedEvent) && !(e instanceof AlgorithmFinishedEvent)){
					MCTSPathSearch.this.post(e); // forward everything
				}
			}
		});
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		switch (this.getState()) {
		case CREATED:

			/* initialize MCTS */
			this.mdp.setLoggerName(this.getLoggerName() + ".mdp");
			IEvent mctsInitEvent;
			do {
				mctsInitEvent = this.mcts.next();
			} while (!(mctsInitEvent instanceof AlgorithmInitializedEvent));
			return this.activate();
		case ACTIVE:

			/* if MCTS has finished, terminate */
			if (this.mcts.getState() != EAlgorithmState.ACTIVE) {
				return this.terminate();
			}

			/* keep asking for playouts until a playout is returned that is a solution path */
			IAlgorithmEvent e;
			while (!((e = this.mcts.nextWithException()) instanceof AlgorithmFinishedEvent)) {

				/* if this is not an event that declares the completion of an iteration, ignore it */
				if (!(e instanceof MCTSIterationCompletedEvent)) {
					continue;
				}

				/* form a path object and return a respective event */
				MCTSIterationCompletedEvent<N, A, Double> ce = (MCTSIterationCompletedEvent<N, A, Double>) e;
				double overallScore = SetUtil.sum(ce.getScores());
				this.logger.info("Registered rollout with score {}. Updating best seen solution correspondingly.", overallScore);
				EvaluatedSearchGraphPath<N, A, Double> path = new EvaluatedSearchGraphPath<>(ce.getRollout(), overallScore);

				/* only if the roll-out is a goal path, emit a success event */
				if (this.getGoalTester().isGoal(path)) {
					this.updateBestSeenSolution(path);
					int hashCode = path.hashCode();
					if (this.hashCodesOfReturnedPaths.contains(hashCode)) {
						this.logger.info("Skipping (and supressing) previously found solution with hash code {}", hashCode);
						continue;
					}
					this.hashCodesOfReturnedPaths.add(hashCode);
					ISolutionCandidateFoundEvent<EvaluatedSearchGraphPath<N, A, Double>> event = new EvaluatedSearchSolutionCandidateFoundEvent<>(this, path);
					this.post(event);
					return event;
				}
			}
			return this.terminate();

		default:
			throw new IllegalStateException();
		}
	}

	@Override
	public void setTimeout(final Timeout to) {
		long toInSeconds = to.seconds();
		if (toInSeconds < 2) {
			throw new IllegalArgumentException("Cannot run MCTS with a timeout of less than 2 seconds.");
		}
		super.setTimeout(to);
		this.mcts.setTimeout(new Timeout(to.seconds() - 1, TimeUnit.SECONDS));
	}

	@Override
	public void cancel() {
		super.cancel();
		this.mcts.cancel(); // forwarding cancel
	}

	@Override
	public void setLoggerName(final String name) {
		super.setLoggerName(name + "._algorithm");
		this.logger = LoggerFactory.getLogger(name);
		this.mcts.setLoggerName(name + ".mcts");
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	public IMDP<N, A, Double> getMdp() {
		return this.mdp;
	}

	public MCTS<N, A> getMcts() {
		return this.mcts;
	}

	public int getNumberOfNodesInMemory() {
		return this.mcts.getNumberOfNodesInMemory();
	}
}
