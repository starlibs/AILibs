package jaicore.search.evaluationproblems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import jaicore.basic.sets.SetUtil.Pair;
import jaicore.graph.LabeledGraph;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.interfaces.IObservableORGraphSearch;
import jaicore.search.algorithms.interfaces.IPathUnification;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.bestfirst.BestFirstEpsilon;
import jaicore.search.algorithms.standard.bestfirst.RandomCompletionEvaluator;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.core.ORGraphSearch;
import jaicore.search.algorithms.standard.mcts.UCT;
import jaicore.search.algorithms.standard.rdfs.RandomizedDepthFirstSearch;
import jaicore.search.evaluationproblems.EnhancedTTSP.EnhancedTTSPNode;
import jaicore.search.structure.core.Node;

public class EnhancedTTSPExample {

	private static final int N = 10;
	private static final int MAX_DISTANCE = 12;
	private static final int TIMEOUT_IN_MS = 5 * 60 * 1000;
	private static final boolean VISUALIZE = true;

	private static EnhancedTTSP ttsp;
	private static LabeledGraph<Short, Double> travelGraph;

	public static void main(String... args) {
		EnhancedTTSPExample example = new EnhancedTTSPExample();
		example.createProblem();
		try {
			example.testRandomHillClimbing();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			example.testRandomCompletor();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			example.testMCTS();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			example.testDijkstra();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			example.testDFS();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			example.testAStarEpsilon();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			example.testAStar();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void createProblem() {

		/* create TTSP problem */
		Random r = new Random(0);
		List<Boolean> blockedHours = Arrays
				.asList(new Boolean[] { true, true, true, true, true, true, false, false, false, false, false, false,
						false, false, false, false, false, false, false, false, false, false, true, true }); // driving
																												// blocked
																												// between
																												// 11pm
																												// and 6
																												// am
		travelGraph = new LabeledGraph<>();
		List<Pair<Double, Double>> coordinates = new ArrayList<>();
		for (short i = 0; i < N; i++) {
			coordinates.add(new Pair<>(r.nextDouble() * MAX_DISTANCE, r.nextDouble() * MAX_DISTANCE));
			travelGraph.addItem(i);
		}
		for (short i = 0; i < N; i++) {
			double x1 = coordinates.get(i).getX();
			double y1 = coordinates.get(i).getY();
			for (short j = 0; j < i; j++) { // we assume a symmetric travel graph
				double x2 = coordinates.get(j).getX();
				double y2 = coordinates.get(j).getY();
				double minTravelTime = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
				// System.out.println("(" + x1 + ", " + y1 + ") to (" +x2 +", " + y2 +") = "
				// +minTravelTime);
				travelGraph.addEdge(i, j, minTravelTime);
				travelGraph.addEdge(j, i, minTravelTime);
			}
		}

		ttsp = new EnhancedTTSP(travelGraph, (short) 0, blockedHours, 8, 4.5, 1, 10);
		System.out.println("Problem created ...");
	}

	public void testRandomHillClimbing() throws Throwable {
		runAlgorithm("Random Hill Climbing", new RandomizedDepthFirstSearch<>(ttsp.getGraphGenerator(), new Random(0)),
				true);
	}

	public void testDFS() throws Throwable {
		runAlgorithm("DFS", new ORGraphSearch<>(ttsp.getGraphGenerator(), n -> n.externalPath().size() * -1.0), false);
	}

	public void testDijkstra() throws Throwable {
		runAlgorithm("Dijkstra", new ORGraphSearch<>(ttsp.getGraphGenerator(), n -> {
			return n.getPoint().getTime();
		}), true);
	}

	/**
	 * Small class for heuristic based node evaluation as can be used by AStar
	 */
	private class AStarNodeEvaluator implements INodeEvaluator<EnhancedTTSPNode, Double> {

		@Override
		public Double f(Node<EnhancedTTSPNode, ?> node) throws Throwable {

			double g = node.getPoint().getTime();
			double h = 0;
			List<Double> edgesOfUncoveredPlaces = travelGraph.getEdges().stream()
					.filter(e -> node.getPoint().getUnvisitedLocations().contains(e.getX())
							&& node.getPoint().getUnvisitedLocations().contains(e.getY()))
					.map(e -> travelGraph.getEdgeLabel(e)).sorted().collect(Collectors.toList());
			int m = node.getPoint().getUnvisitedLocations().size();
			if (m > 1) {
				for (int i = 0; i < m; i++)
					h += edgesOfUncoveredPlaces.get(i);
			} else if (m == 1)
				h = travelGraph.getEdgeLabel(node.getPoint().getCurLocation(), (short) 0);
			return g + h;

		}

	}

	public void testAStar() throws Throwable {
		runAlgorithm("AStar", new BestFirst<>(ttsp.getGraphGenerator(), new AStarNodeEvaluator()), true);
	}

	public void testAStarEpsilon() throws Throwable {
		runAlgorithm("AStarEpsilon", new BestFirstEpsilon<EnhancedTTSPNode, String, Integer>(ttsp.getGraphGenerator(),
				new AStarNodeEvaluator(), n -> n.getPoint().getUnvisitedLocations().size(), 1.3, false), true);
	}

	public void testMCTS() throws Throwable {
		runAlgorithm("MCTS", new UCT<EnhancedTTSPNode, String>(ttsp.getGraphGenerator(),
				n -> ttsp.getSolutionEvaluator().evaluateSolution(n.externalPath()), 0), false);
	}

	public void testRandomCompletor() throws Throwable {
		INodeEvaluator<EnhancedTTSPNode, Double> rc = new RandomCompletionEvaluator<>(new Random(123l), 100,
				new IPathUnification<EnhancedTTSPNode>() {

					@Override
					public List<EnhancedTTSPNode> getSubsumingKnownPathCompletion(
							Map<List<EnhancedTTSPNode>, List<EnhancedTTSPNode>> knownPathCompletions,
							List<EnhancedTTSPNode> path) throws InterruptedException {
						return null;
					}
				}, ttsp.getSolutionEvaluator());
		BestFirst<EnhancedTTSPNode, String> bf = new BestFirst<>(ttsp.getGraphGenerator(), rc);
		bf.setLoggerName("outer");
		runAlgorithm("BFS with random completion", bf, false);
	}

	private void runAlgorithm(String name, IObservableORGraphSearch<EnhancedTTSPNode, String, Double> search,
			boolean stopOnFirst) throws Throwable {
		System.out.println("Running " + name);
		long start = System.currentTimeMillis();
		Pair<List<EnhancedTTSPNode>, Double> answer = runSearch(search, stopOnFirst, VISUALIZE);
		long runtime = System.currentTimeMillis() - start;
		System.out.println("Runtime was: " + runtime);
		System.out.println(
				"Best tour is: " + answer.getX().stream().map(m -> m.getCurLocation()).collect(Collectors.toList()));
		System.out.println("Time of best tour is: " + answer.getY());
	}

	private Pair<List<EnhancedTTSPNode>, Double> runSearch(
			IObservableORGraphSearch<EnhancedTTSPNode, String, Double> search, boolean stopOnFirst, boolean visualize)
			throws Throwable {

		/* activate visualizer if desired */
		if (visualize) {
			SimpleGraphVisualizationWindow<Node<EnhancedTTSPNode, Double>> win = new SimpleGraphVisualizationWindow<>(
					search);
			win.getPanel()
					.setTooltipGenerator(n -> "<table>" + "<tr><td>f-value</td><td>" + n.getInternalLabel()
							+ "</td></tr>" + "<tr><td>f-time</td><td>" + n.getAnnotations() + "</td></tr>"
							+ "<tr><td>Loc</td><td>" + n.getPoint().getCurLocation() + "</td></tr>"
							+ "<tr><td>Unvisited Locations</td><td>" + n.getPoint().getUnvisitedLocations()
							+ "</td></tr>" + "<tr><td>Time</td><td>" + n.getPoint().getTime() + "</td></tr>"
							+ "<tr><td>Time Traveled since last short break</td><td>"
							+ n.getPoint().getTimeTraveledSinceLastShortBreak() + "</td></tr>"
							+ "<tr><td>Time Traveled since last long break</td><td>"
							+ n.getPoint().getTimeTraveledSinceLastLongBreak() + "</td></tr>" + "</table>");
		}

		/* schedule timeout */
		new Timer().schedule(new TimerTask() {

			@Override
			public void run() {
				System.out.println("Canceling algorithm");
				search.cancel();
			}
		}, TIMEOUT_IN_MS);

		/* run search */
		List<EnhancedTTSPNode> bestSolution = null;
		double bestValue = Double.MAX_VALUE;
		List<EnhancedTTSPNode> solution;
		while (!Thread.currentThread().isInterrupted() && (solution = search.nextSolution()) != null) {
			double value = solution.get(solution.size() - 1).getTime();
			if (value < bestValue) {
				bestSolution = solution;
				bestValue = value;
			}
			if (stopOnFirst)
				break;
		}
		return new Pair<>(bestSolution, bestValue);
	}
}
