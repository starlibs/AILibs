package jaicore.search.algorithms.standard.awastar;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.search.algorithms.interfaces.ISolutionEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
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

public class AwaStarSearch<T, A, V extends Comparable<V>>{

	private static final Logger logger = LoggerFactory.getLogger(AwaStarSearch.class);

	private class Search{
		
		private SuccessorGenerator<T, A> successorGenerator;
		private GoalTester<T> goalTester;
		private INodeEvaluator<T, V> nodeEvaluator;
		private ISolutionEvaluator<T, V> solutionEvaluator;
		private OpenCollection<Node<T, V>> closedList, suspendList, openList;
		private int windowSize;
		private V bestScore;
		private List<Node<T, V>> bestSolution;
		private V bestSolutionScore;
		private List<Node<T, V>> bestCompleteSolution;
		
		public Search(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> nodeEvaluator, ISolutionEvaluator<T, V> solutionEvaluator) throws Throwable {
			successorGenerator = graphGenerator.getSuccessorGenerator();
			goalTester = graphGenerator.getGoalTester();
			this.nodeEvaluator = nodeEvaluator;
			this.solutionEvaluator = solutionEvaluator;
			closedList = new PriorityQueueOpen<>();
			suspendList = new PriorityQueueOpen<>();
			openList = new PriorityQueueOpen<>();
			windowSize = 0;
			Node<T, V> rootNode = new Node<T, V>(null, ((SingleRootGenerator<T>)graphGenerator.getRootGenerator()).getRoot());
			rootNode.setInternalLabel(this.nodeEvaluator.f(rootNode));
			rootNode.setAnnotation("level", 0);
			openList.add(rootNode);
			bestScore = null;
			bestSolutionScore = null;
		}

		public List<Node<T, V>> search(long timeout){
			long t = System.currentTimeMillis();
			long end = t + timeout * 1000;
			do {
				closedList.addAll(openList);
				openList.addAll(suspendList);
				suspendList.clear();
				bestSolution = windowAStar();
				windowSize++;
			} while (!suspendList.isEmpty() && System.currentTimeMillis() < end);
			return bestCompleteSolution;
		}
		
		private List<Node<T, V>> windowAStar() {
			int currentLevel = -1;
			while (!openList.isEmpty()) {
				Node<T, V> n = openList.peek();
				openList.remove(n);
				closedList.add(n);
				V nScore = n.getInternalLabel();
				int nLevel = (int) n.getAnnotation("level");
				if (nScore != null && bestScore != null && nScore.compareTo(bestScore) >= 0) {
					return bestSolution;
				} else {
					if (nLevel <= (currentLevel - windowSize)) {
						closedList.remove(n);
						suspendList.add(n);
					} else {
						if (nLevel > currentLevel) {
							currentLevel = nLevel;
						}
						if (n.isGoal()) {
							n.setGoal(true);
							bestScore = n.getInternalLabel();
							bestSolution = n.path();
							V solutionScore;
							try {
								solutionScore = solutionEvaluator.evaluateSolution(n.externalPath());
								if (solutionScore != null && bestSolutionScore != null && bestSolutionScore.compareTo(solutionScore) <= 0) {
									bestSolutionScore = solutionScore;
									bestCompleteSolution = n.path();
								} else if (solutionScore != null && bestSolutionScore == null) {
									bestSolutionScore = solutionScore;
									bestCompleteSolution = n.path();
								}
							} catch (Exception e) {
								logger.warn(e.getMessage());
							}
							return bestSolution;
						}
						Collection<NodeExpansionDescription<T, A>> successors = successorGenerator.generateSuccessors(n.getPoint());
						for (NodeExpansionDescription<T, A> expansionDescription : successors) {
							Node<T, V> nPrime = new Node<>(n, expansionDescription.getTo());
							if (goalTester instanceof NodeGoalTester<?>) {
								nPrime.setGoal(((NodeGoalTester<T>)goalTester).isGoal(nPrime.getPoint()));
							} else if (goalTester instanceof PathGoalTester<?>) {
								nPrime.setGoal(((PathGoalTester<T>) goalTester).isGoal(nPrime.externalPath()));
							}
							V nPrimeScore;
							try {
								nPrimeScore = nodeEvaluator.f(nPrime);
								if (!openList.contains(nPrime) && !closedList.contains(nPrime) && !suspendList.contains(nPrime)) {
									nPrime.setParent(n);
									nPrime.setInternalLabel(nPrimeScore);
									nPrime.setAnnotation("level", ((int)n.getAnnotation("level")) + 1);
									openList.add(nPrime);
								} else if(openList.contains(nPrime) || suspendList.contains(nPrime)) {
									V oldScore = nPrime.getInternalLabel();
									if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
											nPrime.setParent(n);
											nPrime.setInternalLabel(nPrimeScore);
											nPrime.setAnnotation("level", ((int)n.getAnnotation("level")) + 1);
									}
								} else if(closedList.contains(nPrime)) {
									V oldScore = nPrime.getInternalLabel();
									if (oldScore != null && oldScore.compareTo(nPrimeScore) > 0) {
										nPrime.setParent(n);
										nPrime.setInternalLabel(nPrimeScore);
										nPrime.setAnnotation("level", ((int)n.getAnnotation("level")) + 1);
									}
									openList.add(nPrime);
								}
								
							} catch (Throwable e) {
								logger.error(e.getMessage());
							}
						}
					}
				}
			}
			return bestSolution;
		}

	}

	private Search search; 
	
	public AwaStarSearch(GraphGenerator<T, A> graphGenerator, INodeEvaluator<T, V> nodeEvaluator, ISolutionEvaluator<T, V> solutionEvaluator) throws Throwable {
		this.search = new Search(graphGenerator, nodeEvaluator, solutionEvaluator);
	}

	public List<Node<T, V>> search(int timeout) {
		return search.search(timeout);
	}

}
