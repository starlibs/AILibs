package jaicore.search.algorithms.standard.awastar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import jaicore.graphvisualizer.events.graphEvents.GraphInitializedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeReachedEvent;
import jaicore.graphvisualizer.events.graphEvents.NodeTypeSwitchEvent;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.INodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.ISolutionReportingNodeEvaluator;
import jaicore.search.algorithms.standard.core.events.SolutionFoundEvent;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.core.NodeExpansionDescription;
import jaicore.search.structure.core.OpenCollection;
import jaicore.search.structure.core.PriorityQueueOpen;
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
public class AwaStarSearch<T, A, V extends Comparable<V>> implements IObservableORGraphSearch<T, A, V> {

	private static final Logger logger = LoggerFactory.getLogger(AwaStarSearch.class);

	private final EventBus eventBus = new EventBus();
	private boolean stopped = false;

	private class Search {

		private SingleRootGenerator<T> rootNodeGenerator;
		private SuccessorGenerator<T, A> successorGenerator;
		private GoalTester<T> goalTester;
		private INodeEvaluator<T, V> nodeEvaluator;
		private OpenCollection<Node<T, V>> closedList, suspendList, openList;
		private int currentLevel = -1;
		private int windowSize;
		private V bestScore;
		private List<List<T>> unreturnedSolutions = new ArrayList<>();
		private List<T> bestSolution;

		private boolean initialized = false;

		public Search(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> nodeEvaluator) throws Throwable {
			rootNodeGenerator = (SingleRootGenerator<T>) graphGenerator.getRootGenerator();
			successorGenerator = graphGenerator.getSuccessorGenerator();
			goalTester = graphGenerator.getGoalTester();
			this.nodeEvaluator = nodeEvaluator;
			closedList = new PriorityQueueOpen<>();
			suspendList = new PriorityQueueOpen<>();
			openList = new PriorityQueueOpen<>();
			windowSize = 0;
			bestScore = null;
			if (nodeEvaluator instanceof ISolutionReportingNodeEvaluator) {
				((ISolutionReportingNodeEvaluator) nodeEvaluator).registerSolutionListener(this);
			}
		}

		public List<T> nextSolution() throws Throwable {

			logger.info("Searching for next solution.");

			/* initialize search if not happened already */
			if (!initialized) {
				initialized = true;
				T externalRootNode = rootNodeGenerator.getRoot();
				Node<T, V> rootNode = new Node<T, V>(null, externalRootNode);
				logger.info("Initializing graph and OPEN with {}.", rootNode);
				openList.add(rootNode);
				eventBus.post(new GraphInitializedEvent<>(rootNode));
				rootNode.setInternalLabel(this.nodeEvaluator.f(rootNode));
			}

			/* return pending solutions if there are any */
			while (unreturnedSolutions.isEmpty()) {

				/* check whether execution shoud be halted */
				if (Thread.interrupted()) {
					throw new InterruptedException("AWA* has been interrupted");
				} else if (stopped) {
					return null;
				}

				/* if the current graph has been exhausted, add all suspended nodes to OPEN and increase window size */
				if (openList.isEmpty()) {
					if (suspendList.isEmpty()) {
						logger.info("The whole graph has been exhausted. No more solutions can be found!");
						return null;
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
			List<T> toReturn = unreturnedSolutions.get(0);
			unreturnedSolutions.remove(0);
			return toReturn;
		}

		private void windowAStar() throws Throwable {
			while (!openList.isEmpty()) {
				if (!unreturnedSolutions.isEmpty()) {
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
							registerNewSolution(newSolution, nPrimeScore);
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
		public void receiveSolutionEvent(SolutionFoundEvent<T, V> solutionEvent) {
			registerNewSolution(solutionEvent.getSolution(), solutionEvent.getF());
		}

		public void registerNewSolution(List<T> solution, V score) {
			if (bestScore == null || score.compareTo(bestScore) < 0) {
				logger.info("Identified new best solution {} with quality {}", solution, score);
				bestScore = score;
				bestSolution = solution;
			} else
				logger.info("Identified new solution {} with quality {}", solution, score);
			unreturnedSolutions.add(solution);
		}
	}

	public List<T> getBestSolution() {
		return search.bestSolution;
	}

	private Search search;

	public AwaStarSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> nodeEvaluator) throws Throwable {
		this.search = new Search(graphGenerator, nodeEvaluator);
	}

	public List<T> nextSolution() throws Throwable {
		return search.nextSolution();
	}

	public void gatherSolutions() throws Throwable {
		while (nextSolution() != null)
			;
	}

	@Override
	public void bootstrap(Collection<Node<T, V>> nodes) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V getFValue(T node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V getFValue(Node<T, V> node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getAnnotationsOfReturnedSolution(List<T> solution) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getAnnotationOfReturnedSolution(List<T> solution, String annotation) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V getFOfReturnedSolution(List<T> solution) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void cancel() {
		this.stopped = true;
	}

	@Override
	public Node<T, V> getInternalRepresentationOf(T node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Node<T, V>> getOpenSnapshot() {
		throw new UnsupportedOperationException();
	}

	@Override
	public GraphGenerator<T, A> getGraphGenerator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public INodeEvaluator<T, V> getNodeEvaluator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void registerListener(Object listener) {
		eventBus.register(listener);
	}
}
