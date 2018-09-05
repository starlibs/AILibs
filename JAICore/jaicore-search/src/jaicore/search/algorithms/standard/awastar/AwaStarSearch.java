package jaicore.search.algorithms.standard.awastar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.search.algorithms.standard.AbstractORGraphSearch;
import jaicore.search.algorithms.standard.bestfirst.events.GraphSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.model.OpenCollection;
import jaicore.search.algorithms.standard.bestfirst.model.PriorityQueueOpen;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.ISolutionReportingNodeEvaluator;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.other.EvaluatedSearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

/**
 * This is a modified version of the AWA* algorithm for problems without admissible heuristic. Important differences are: - no early termination if a best-f-valued solution is found as f is not optimistic
 * 
 * @author lbrandt2 and fmohr
 *
 * @param <T>
 * @param <A>
 * @param <V>
 */
public class AwaStarSearch<I extends GeneralEvaluatedTraversalTree<T, A, V>, T, A, V extends Comparable<V>> extends AbstractORGraphSearch<I, EvaluatedSearchGraphPath<T, A, V>, T, A, V, Node<T, V>, A> {

	private static final Logger logger = LoggerFactory.getLogger(AwaStarSearch.class);

	private final EventBus eventBus = new EventBus();
	private boolean stopped = false;

	private AlgorithmState state = AlgorithmState.created;
	
	private SingleRootGenerator<T> rootNodeGenerator;
	private SuccessorGenerator<T, A> successorGenerator;
	private GoalTester<T> goalTester;
	private INodeEvaluator<T, V> nodeEvaluator;
	private OpenCollection<Node<T, V>> closedList, suspendList, openList;
	private int currentLevel = -1;
	private int windowSize;
	private List<GraphSearchSolutionCandidateFoundEvent<T, A, V>> unreturnedSolutionEvents = new ArrayList<>();
	private EvaluatedSearchGraphPath<T, A, V> bestSolution;
	
	public AwaStarSearch(I problem) {
		super(problem);
		rootNodeGenerator = (SingleRootGenerator<T>) problem.getGraphGenerator().getRootGenerator();
		successorGenerator = problem.getGraphGenerator().getSuccessorGenerator();
		goalTester = problem.getGraphGenerator().getGoalTester();
		nodeEvaluator = problem.getNodeEvaluator();
		closedList = new PriorityQueueOpen<>();
		suspendList = new PriorityQueueOpen<>();
		openList = new PriorityQueueOpen<>();
		windowSize = 0;
		if (nodeEvaluator instanceof ISolutionReportingNodeEvaluator) {
			((ISolutionReportingNodeEvaluator) nodeEvaluator).registerSolutionListener(this);
		}
	}

	private AlgorithmEvent processUntilNextEvent() throws Exception {

		logger.info("Searching for next solution.");

		/* return pending solutions if there are any */
		while (unreturnedSolutionEvents.isEmpty()) {

			/* check whether execution shoud be halted */
			if (Thread.interrupted()) {
				throw new InterruptedException("AWA* has been interrupted");
			} else if (stopped) {
				return new AlgorithmFinishedEvent();
			}

			/* if the current graph has been exhausted, add all suspended nodes to OPEN and increase window size */
			if (openList.isEmpty()) {
				if (suspendList.isEmpty()) {
					logger.info("The whole graph has been exhausted. No more solutions can be found!");
					return new AlgorithmFinishedEvent();
				} else {
					logger.info("Search with window size {} is exhausted. Reactivating {} suspended nodes and incrementing window size.", windowSize, suspendList.size());
					openList.addAll(suspendList);
					suspendList.clear();
					windowSize++;
					currentLevel = -1;
				}
			}
			logger.info("Running core algorithm with window size {} and current level {}. {} items are in OPEN", windowSize, currentLevel, openList.size());
			windowAStar();
		}
		GraphSearchSolutionCandidateFoundEvent<T, A, V> toReturn = unreturnedSolutionEvents.get(0);
		unreturnedSolutionEvents.remove(0);
		return toReturn;
	}

	private void windowAStar() throws Exception {
		while (!openList.isEmpty()) {
			if (!unreturnedSolutionEvents.isEmpty()) {
				logger.info("Not doing anything because there are still unreturned solutions.");
				return;
			}
			if (stopped) {
				logger.info("Algorithm execution has been halted");
				return;
			}
			Node<T, V> n = openList.peek();
			openList.remove(n);
			closedList.add(n);
			if (!n.isGoal())
				eventBus.post(new NodeTypeSwitchEvent<>(n, "or_closed"));

			/* check whether this node is outside the window and suspend it */
			int nLevel = n.externalPath().size() - 1;
			if (nLevel <= (currentLevel - windowSize)) {
				closedList.remove(n);
				suspendList.add(n);
				logger.info("Suspending node {} with level {}, which is lower than {}", n, nLevel, currentLevel - windowSize);
				eventBus.post(new NodeTypeSwitchEvent<>(n, "or_suspended"));
				continue;
			}

			/* if the level should even be increased, do this now */
			if (nLevel > currentLevel) {
				logger.info("Switching level from {} to {}", currentLevel, nLevel);
				currentLevel = nLevel;
			}
			if (stopped) {
				logger.info("Algorithm execution has been halted");
				return;
			}

			/* compute successors of the expanded node */
			Collection<NodeExpansionDescription<T, A>> successors = successorGenerator.generateSuccessors(n.getPoint());
			logger.info("Expanding {}. Identified {} successors.", n.getPoint(), successors.size());
			for (NodeExpansionDescription<T, A> expansionDescription : successors) {
				if (Thread.interrupted()) {
					throw new InterruptedException("AWA* has been interrupted.");
				}
				if (stopped) {
					logger.info("Algorithm execution has been halted");
					return;
				}
				Node<T, V> nPrime = new Node<>(n, expansionDescription.getTo());
				if (goalTester instanceof NodeGoalTester<?>) {
					nPrime.setGoal(((NodeGoalTester<T>) goalTester).isGoal(nPrime.getPoint()));
				} else if (goalTester instanceof PathGoalTester<?>) {
					nPrime.setGoal(((PathGoalTester<T>) goalTester).isGoal(nPrime.externalPath()));
				}
				V nPrimeScore;
				try {
					nPrimeScore = nodeEvaluator.f(nPrime);

					/* ignore nodes whose value cannot be determined */
					if (nPrimeScore == null) {
						logger.debug("Discarding node {} for which no f-value could be computed.", nPrime);
						continue;
					}

					/* determine whether this is a goal node */
					if (nPrime.isGoal()) {
						List<T> newSolution = nPrime.externalPath();
						registerNewSolutionCandidate(new EvaluatedSearchGraphPath<>(newSolution, null, nPrimeScore));
					}

					if (!openList.contains(nPrime) && !closedList.contains(nPrime) && !suspendList.contains(nPrime)) {
						nPrime.setParent(n);
						nPrime.setInternalLabel(nPrimeScore);
						if (!nPrime.isGoal())
							openList.add(nPrime);
						eventBus.post(new NodeReachedEvent<>(n, nPrime, nPrime.isGoal() ? "or_solution" : "or_open"));
					} else if (openList.contains(nPrime) || suspendList.contains(nPrime)) {
						V oldScore = nPrime.getInternalLabel();
						if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
							nPrime.setParent(n);
							nPrime.setInternalLabel(nPrimeScore);
						}
					} else if (closedList.contains(nPrime)) {
						V oldScore = nPrime.getInternalLabel();
						if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
							nPrime.setParent(n);
							nPrime.setInternalLabel(nPrimeScore);
						}
						if (!nPrime.isGoal())
							openList.add(nPrime);
					}

				} catch (InterruptedException e) {
					throw e;
				} catch (Throwable e) {
					logger.error(e.getClass().getName() + ": " + e.getMessage());
				}
			}
		}
	}

	@Subscribe
	public void receiveSolutionEvent(GraphSearchSolutionCandidateFoundEvent<T, A, V> solutionEvent) {
		registerNewSolutionCandidate(solutionEvent.getSolutionCandidate());
	}

	public void registerNewSolutionCandidate(EvaluatedSearchGraphPath<T, A, V> solution) {
		List<T> solutionPath = solution.getNodes();
		V score = solution.getScore();
		GraphSearchSolutionCandidateFoundEvent<T, A, V> solutionEvent = new GraphSearchSolutionCandidateFoundEvent<>(solution);
		eventBus.post(solutionEvent);
		if (bestSolution == null || score.compareTo(bestSolution.getScore()) < 0) {
			logger.info("Identified new best solution {} with quality {}", solutionPath, score);
			bestSolution = solution;
		} else
			logger.info("Identified new solution {} with quality {}", solutionPath, score);
		unreturnedSolutionEvents.add(solutionEvent);
	}

	public EvaluatedSearchGraphPath<T, A, V> getBestSolution() {
		return bestSolution;
	}
	
	@Override
	public void cancel() {
		this.stopped = true;
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
		return state != AlgorithmState.inactive;
	}

	@Override
	public AlgorithmEvent next() {
		try {
			switch (state) {
			case created: {
				T externalRootNode = rootNodeGenerator.getRoot();
				Node<T, V> rootNode = new Node<T, V>(null, externalRootNode);
				logger.info("Initializing graph and OPEN with {}.", rootNode);
				openList.add(rootNode);
				eventBus.post(new GraphInitializedEvent<>(rootNode));
				rootNode.setInternalLabel(this.nodeEvaluator.f(rootNode));
				state = AlgorithmState.active;
				return new AlgorithmInitializedEvent();
			}
			case active: {
				AlgorithmEvent event = processUntilNextEvent();
				if (event instanceof AlgorithmFinishedEvent) {
					state = AlgorithmState.inactive;
				}
				return event;
			}
			default:
				throw new IllegalStateException("Cannot do anything in state " + state);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public EvaluatedSearchGraphPath<T, A, V> call() throws Exception {
		while (this.hasNext())
			next();
		return getBestSolution();
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		logger.warn("Currently no support for parallelization");
	}

	@Override
	public int getNumCPUs() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeout(int timeout, TimeUnit timeUnit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TimeUnit getTimeoutUnit() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphGenerator<T, A> getGraphGenerator() {
		return problem.getGraphGenerator();
	}
}
