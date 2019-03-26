package jaicore.search.algorithms.standard.awastar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.graphvisualizer.events.graph.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graph.NodeAddedEvent;
import jaicore.graphvisualizer.events.graph.NodeTypeSwitchEvent;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.ICancelableNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.IPotentiallySolutionReportingNodeEvaluator;
import jaicore.search.core.interfaces.AOptimalPathInORGraphSearch;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * This is a modified version of the AWA* algorithm for problems without admissible heuristic. Important differences are: - no early termination if a best-f-valued solution is found as f is not
 * optimistic
 *
 * @author lbrandt2 and fmohr
 *
 * @param <T>
 * @param <A>
 * @param <V>
 */
public class AwaStarSearch<I extends GraphSearchWithSubpathEvaluationsInput<T, A, V>, T, A, V extends Comparable<V>> extends AOptimalPathInORGraphSearch<I, T, A, V> {

	private Logger logger = LoggerFactory.getLogger(AwaStarSearch.class);
	private String loggerName;

	private final SingleRootGenerator<T> rootNodeGenerator;
	private final SuccessorGenerator<T, A> successorGenerator;
	private final GoalTester<T> goalTester;
	private final INodeEvaluator<T, V> nodeEvaluator;
	private final Queue<Node<T, V>> closedList;
	private final Queue<Node<T, V>> suspendList;
	private final Queue<Node<T, V>> openList;
	private int currentLevel = -1;
	private int windowSize;
	private final List<EvaluatedSearchGraphPath<T, A, V>> unconfirmedSolutions = new ArrayList<>(); // these are solutions emitted on the basis of the node evaluator but whose solutions have not been found in the original graph yet
	private final List<EvaluatedSearchSolutionCandidateFoundEvent<T, A, V>> unreturnedSolutionEvents = new ArrayList<>();

	@SuppressWarnings("rawtypes")
	public AwaStarSearch(final I problem) {
		super(problem);
		this.rootNodeGenerator = (SingleRootGenerator<T>) problem.getGraphGenerator().getRootGenerator();
		this.successorGenerator = problem.getGraphGenerator().getSuccessorGenerator();
		this.goalTester = problem.getGraphGenerator().getGoalTester();
		this.nodeEvaluator = problem.getNodeEvaluator();

		this.closedList = new PriorityQueue<>();
		this.suspendList = new PriorityQueue<>();
		this.openList = new PriorityQueue<>();
		this.windowSize = 0;
		if (this.nodeEvaluator instanceof IPotentiallySolutionReportingNodeEvaluator) {
			((IPotentiallySolutionReportingNodeEvaluator) this.nodeEvaluator).registerSolutionListener(this);
		}
	}

	private void windowAStar() throws NodeEvaluationException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, InterruptedException {
		while (!this.openList.isEmpty()) {
			this.checkAndConductTermination();
			if (!this.unreturnedSolutionEvents.isEmpty()) {
				this.logger.info("Not doing anything because there are still unreturned solutions.");
				return;
			}
			Node<T, V> n = this.openList.peek();
			this.openList.remove(n);
			this.closedList.add(n);
			if (!n.isGoal()) {
				this.post(new NodeTypeSwitchEvent<>(this.getId(), n, "or_closed"));
			}

			/* check whether this node is outside the window and suspend it */
			int nLevel = n.externalPath().size() - 1;
			if (nLevel <= (this.currentLevel - this.windowSize)) {
				this.closedList.remove(n);
				this.suspendList.add(n);
				this.logger.info("Suspending node {} with level {}, which is lower than {}", n, nLevel, this.currentLevel - this.windowSize);
				this.post(new NodeTypeSwitchEvent<>(this.getId(), n, "or_suspended"));
				continue;
			}

			/* if the level should even be increased, do this now */
			if (nLevel > this.currentLevel) {
				this.logger.info("Switching level from {} to {}", this.currentLevel, nLevel);
				this.currentLevel = nLevel;
			}
			this.checkAndConductTermination();

			/* compute successors of the expanded node */
			Collection<NodeExpansionDescription<T, A>> successors = this.successorGenerator.generateSuccessors(n.getPoint());
			this.logger.info("Expanding {}. Identified {} successors.", n.getPoint(), successors.size());
			for (NodeExpansionDescription<T, A> expansionDescription : successors) {
				this.checkAndConductTermination();
				Node<T, V> nPrime = new Node<>(n, expansionDescription.getTo());
				if (this.goalTester instanceof NodeGoalTester<?>) {
					nPrime.setGoal(((NodeGoalTester<T>) this.goalTester).isGoal(nPrime.getPoint()));
				} else if (this.goalTester instanceof PathGoalTester<?>) {
					nPrime.setGoal(((PathGoalTester<T>) this.goalTester).isGoal(nPrime.externalPath()));
				}
				V nPrimeScore = this.nodeEvaluator.f(nPrime);

				/* ignore nodes whose value cannot be determined */
				if (nPrimeScore == null) {
					this.logger.debug("Discarding node {} for which no f-value could be computed.", nPrime);
					continue;
				}

				/* determine whether this is a goal node */
				if (nPrime.isGoal()) {
					List<T> newSolution = nPrime.externalPath();
					EvaluatedSearchGraphPath<T, A, V> solution = new EvaluatedSearchGraphPath<>(newSolution, null, nPrimeScore);
					this.registerNewSolutionCandidate(solution);
				}

				if (!this.openList.contains(nPrime) && !this.closedList.contains(nPrime) && !this.suspendList.contains(nPrime)) {
					nPrime.setParent(n);
					nPrime.setInternalLabel(nPrimeScore);
					if (!nPrime.isGoal()) {
						this.openList.add(nPrime);
					}
					this.post(new NodeAddedEvent<>(this.getId(), n, nPrime, nPrime.isGoal() ? "or_solution" : "or_open"));
				} else if (this.openList.contains(nPrime) || this.suspendList.contains(nPrime)) {
					V oldScore = nPrime.getInternalLabel();
					if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
						nPrime.setParent(n);
						nPrime.setInternalLabel(nPrimeScore);
					}
				} else if (this.closedList.contains(nPrime)) {
					V oldScore = nPrime.getInternalLabel();
					if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
						nPrime.setParent(n);
						nPrime.setInternalLabel(nPrimeScore);
					}
					if (!nPrime.isGoal()) {
						this.openList.add(nPrime);
					}
				}
			}
		}
	}

	@Subscribe
	public void receiveSolutionEvent(final EvaluatedSearchSolutionCandidateFoundEvent<T, A, V> solutionEvent) {
		this.registerNewSolutionCandidate(solutionEvent.getSolutionCandidate());
		this.unconfirmedSolutions.add(solutionEvent.getSolutionCandidate());
	}

	public EvaluatedSearchSolutionCandidateFoundEvent<T, A, V> registerNewSolutionCandidate(final EvaluatedSearchGraphPath<T, A, V> solution) {
		EvaluatedSearchSolutionCandidateFoundEvent<T, A, V> event = this.registerSolution(solution);
		this.unreturnedSolutionEvents.add(event);
		return event;
	}

	@Override
	public AlgorithmEvent nextWithException() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, NodeEvaluationException {
		this.logger.debug("Next step in {}. State is {}", this.getId(), this.getState());
		this.checkAndConductTermination();
		switch (this.getState()) {
		case created:
			T externalRootNode = this.rootNodeGenerator.getRoot();
			Node<T, V> rootNode = new Node<>(null, externalRootNode);
			this.logger.info("Initializing graph and OPEN with {}.", rootNode);
			this.openList.add(rootNode);
			this.post(new GraphInitializedEvent<>(this.getId(), rootNode));
			rootNode.setInternalLabel(this.nodeEvaluator.f(rootNode));
			return this.activate();

		case active:
			AlgorithmEvent event;
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

	@Override
	protected void shutdown() {

		if (this.isShutdownInitialized()) {
			return;
		}

		/* set state to inactive*/
		this.logger.info("Invoking shutdown routine ...");

		super.shutdown();

		/* cancel node evaluator */
		if (this.nodeEvaluator instanceof ICancelableNodeEvaluator) {
			this.logger.info("Canceling node evaluator.");
			((ICancelableNodeEvaluator) this.nodeEvaluator).cancelActiveTasks();
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
	public GraphGenerator<T, A> getGraphGenerator() {
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
