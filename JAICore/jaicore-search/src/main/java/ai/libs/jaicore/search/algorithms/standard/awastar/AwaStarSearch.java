package ai.libs.jaicore.search.algorithms.standard.awastar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.api4.java.ai.graphsearch.problem.implicit.graphgenerator.IPathGoalTester;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.ICancelablePathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.IPotentiallySolutionReportingPathEvaluator;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.datastructure.graph.implicit.IGraphGenerator;
import org.api4.java.datastructure.graph.implicit.INewNodeDescription;
import org.api4.java.datastructure.graph.implicit.ISingleRootGenerator;
import org.api4.java.datastructure.graph.implicit.ISuccessorGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import ai.libs.jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import ai.libs.jaicore.search.model.other.EvaluatedSearchGraphPath;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;
import ai.libs.jaicore.search.model.travesaltree.DefaultNodeComparator;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;

/**
 * This is a modified version of the AWA* algorithm for problems without admissible heuristic.
 * Important differences are:
 *  - no early termination if a best-f-valued solution is found as f is not optimistic
 *
 * @inproceedings{
 *   title={AWA*-A Window Constrained Anytime Heuristic Search Algorithm.},
 *   author={Aine, Sandip and Chakrabarti, PP and Kumar, Rajeev},
 *   booktitle={IJCAI},
 *   pages={2250--2255},
 *   year={2007}
 * }
 *
 * @author lbrandt2 and fmohr
 *
 * @param <N>
 * @param <A>
 * @param <V>
 */
public class AwaStarSearch<I extends GraphSearchWithSubpathEvaluationsInput<N, A, V>, N, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, N, A, V> {

	private Logger logger = LoggerFactory.getLogger(AwaStarSearch.class);
	private String loggerName;

	private final ISingleRootGenerator<N> rootNodeGenerator;
	private final ISuccessorGenerator<N, A> successorGenerator;
	private final IPathGoalTester<N, A> goalTester;
	private final IPathEvaluator<N, A, V> nodeEvaluator;
	private final Queue<BackPointerPath<N, A, V>> closedList;
	private final Queue<BackPointerPath<N, A, V>> suspendList;
	private final Queue<BackPointerPath<N, A, V>> openList;
	private int currentLevel = -1;
	private int windowSize;
	private final List<EvaluatedSearchGraphPath<N, A, V>> unconfirmedSolutions = new ArrayList<>(); // these are solutions emitted on the basis of the node evaluator but whose solutions have not been found in the original graph yet
	private final List<EvaluatedSearchSolutionCandidateFoundEvent<N, A, V>> unreturnedSolutionEvents = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	public AwaStarSearch(final I problem) {
		super(problem);
		this.rootNodeGenerator = (ISingleRootGenerator<N>) problem.getGraphGenerator().getRootGenerator();
		this.successorGenerator = problem.getGraphGenerator().getSuccessorGenerator();
		this.goalTester = problem.getGoalTester();
		this.nodeEvaluator = problem.getPathEvaluator();

		this.closedList = new PriorityQueue<>(new DefaultNodeComparator<>());
		this.suspendList = new PriorityQueue<>(new DefaultNodeComparator<>());
		this.openList = new PriorityQueue<>(new DefaultNodeComparator<>());
		this.windowSize = 0;
		if (this.nodeEvaluator instanceof IPotentiallySolutionReportingPathEvaluator) {
			((IPotentiallySolutionReportingPathEvaluator) this.nodeEvaluator).registerSolutionListener(this);
		}
	}

	private void windowAStar() throws AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException, AlgorithmException, PathEvaluationException {
		while (!this.openList.isEmpty()) {
			this.checkAndConductTermination();
			if (!this.unreturnedSolutionEvents.isEmpty()) {
				this.logger.info("Not doing anything because there are still unreturned solutions.");
				return;
			}
			BackPointerPath<N, A, V> n = this.openList.peek();
			this.openList.remove(n);
			this.closedList.add(n);
			if (!n.isGoal()) {
				this.post(new NodeTypeSwitchEvent<>(this, n, "or_closed"));
			}

			/* check whether this node is outside the window and suspend it */
			int nLevel = n.getNodes().size() - 1;
			if (nLevel <= (this.currentLevel - this.windowSize)) {
				this.closedList.remove(n);
				this.suspendList.add(n);
				this.logger.info("Suspending node {} with level {}, which is lower than {}", n, nLevel, this.currentLevel - this.windowSize);
				this.post(new NodeTypeSwitchEvent<>(this, n, "or_suspended"));
				continue;
			}

			/* if the level should even be increased, do this now */
			if (nLevel > this.currentLevel) {
				this.logger.info("Switching level from {} to {}", this.currentLevel, nLevel);
				this.currentLevel = nLevel;
			}
			this.checkAndConductTermination();

			/* compute successors of the expanded node */
			this.logger.debug("Expanding {}. Starting successor generation.", n.getHead());
			Collection<INewNodeDescription<N, A>> successors = this.computeTimeoutAware(() -> this.successorGenerator.generateSuccessors(n.getHead()), "Successor generation timeouted" , true);
			this.logger.debug("Successor generation finished. Identified {} successors.", successors.size());
			for (INewNodeDescription<N, A> expansionDescription : successors) {
				this.checkAndConductTermination();
				BackPointerPath<N, A, V> nPrime = new BackPointerPath<>(n, expansionDescription.getTo(), expansionDescription.getArcLabel());
				nPrime.setGoal(this.goalTester.isGoal(nPrime));
				V nPrimeScore = this.nodeEvaluator.evaluate(nPrime);

				/* ignore nodes whose value cannot be determined */
				if (nPrimeScore == null) {
					this.logger.debug("Discarding node {} for which no f-value could be computed.", nPrime);
					continue;
				}

				/* determine whether this is a goal node */
				if (nPrime.isGoal()) {
					EvaluatedSearchGraphPath<N, A, V> solution = new EvaluatedSearchGraphPath<>(nPrime, nPrimeScore);
					this.registerNewSolutionCandidate(solution);
				}

				if (!this.openList.contains(nPrime) && !this.closedList.contains(nPrime) && !this.suspendList.contains(nPrime)) {
					nPrime.setParent(n);
					nPrime.setScore(nPrimeScore);
					if (!nPrime.isGoal()) {
						this.openList.add(nPrime);
					}
					this.post(new NodeAddedEvent<>(this, n, nPrime, nPrime.isGoal() ? "or_solution" : "or_open"));
				} else if (this.openList.contains(nPrime) || this.suspendList.contains(nPrime)) {
					V oldScore = nPrime.getScore();
					if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
						nPrime.setParent(n);
						nPrime.setScore(nPrimeScore);
					}
				} else if (this.closedList.contains(nPrime)) {
					V oldScore = nPrime.getScore();
					if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
						nPrime.setParent(n);
						nPrime.setScore(nPrimeScore);
					}
					if (!nPrime.isGoal()) {
						this.openList.add(nPrime);
					}
				}
			}
		}
	}

	@Subscribe
	public void receiveSolutionEvent(final EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> solutionEvent) {
		this.registerNewSolutionCandidate(solutionEvent.getSolutionCandidate());
		this.unconfirmedSolutions.add(solutionEvent.getSolutionCandidate());
	}

	public EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> registerNewSolutionCandidate(final EvaluatedSearchGraphPath<N, A, V> solution) {
		EvaluatedSearchSolutionCandidateFoundEvent<N, A, V> event = this.registerSolution(solution);
		this.unreturnedSolutionEvents.add(event);
		return event;
	}

	@Override
	public IAlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException{
		try {
			this.registerActiveThread();
			this.logger.debug("Next step in {}. State is {}", this.getId(), this.getState());
			this.checkAndConductTermination();
			switch (this.getState()) {
			case CREATED:
				N externalRootNode = this.rootNodeGenerator.getRoot();
				BackPointerPath<N, A, V> rootNode = new BackPointerPath<>(null, externalRootNode, null);
				this.logger.info("Initializing graph and OPEN with {}.", rootNode);
				this.openList.add(rootNode);
				this.post(new GraphInitializedEvent<>(this, rootNode));
				rootNode.setScore(this.nodeEvaluator.evaluate(rootNode));
				return this.activate();

			case ACTIVE:
				IAlgorithmEvent event;
				this.logger.info("Searching for next solution.");

				/* return pending solutions if there are any */
				while (this.unreturnedSolutionEvents.isEmpty()) {
					this.checkAndConductTermination();

					/* if the current graph has been exhausted, add all suspended nodes to OPEN and increase window size */
					if (this.openList.isEmpty()) {
						if (this.suspendList.isEmpty()) {
							this.logger.info("The whole graph has been exhausted. No more solutions can be found!");
							return this.terminate();
						} else {
							this.logger.info("Search with window size {} is exhausted. Reactivating {} suspended nodes and incrementing window size.", this.windowSize, this.suspendList.size());
							this.openList.addAll(this.suspendList);
							this.suspendList.clear();
							this.windowSize++;
							this.currentLevel = -1;
						}
					}
					this.logger.info("Running core algorithm with window size {} and current level {}. {} items are in OPEN", this.windowSize, this.currentLevel, this.openList.size());
					this.windowAStar();
				}

				/* if we reached this point, there is at least one item in the result list. We return it */
				event = this.unreturnedSolutionEvents.get(0);
				this.unreturnedSolutionEvents.remove(0);
				if (!(event instanceof GraphSearchSolutionCandidateFoundEvent)) { // solution events are sent directly over the event bus
					this.post(event);
				}
				return event;

			default:
				throw new IllegalStateException("Cannot do anything in state " + this.getState());
			}
		}
		catch (PathEvaluationException e) {
			throw new AlgorithmException("Algorithm failed due to path evaluation exception.", e);
		}
		finally {
			this.unregisterActiveThread();
		}
	}

	@Override
	protected void shutdown() {

		if (this.isShutdownInitialized()) {
			return;
		}

		/* set state to inactive*/
		this.logger.info("Invoking shutdown routine ...");

		super.shutdown();

		/* cancel node evaluator */
		if (this.nodeEvaluator instanceof ICancelablePathEvaluator) {
			this.logger.info("Canceling node evaluator.");
			((ICancelablePathEvaluator) this.nodeEvaluator).cancelActiveTasks();
		}

	}

	@Override
	public void setNumCPUs(final int numberOfCPUs) {
		this.logger.warn("Currently no support for parallelization");
	}

	@Override
	public int getNumCPUs() {
		return 1;
	}

	@Override
	public IGraphGenerator<N, A> getGraphGenerator() {
		return this.getInput().getGraphGenerator();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger to {}", name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Switched to logger {}", name);
		super.setLoggerName(this.loggerName + "._orgraphsearch");
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}
}
