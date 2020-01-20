package ai.libs.jaicore.search.algorithms.standard.mcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.graph.Graph;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.RolloutEvent;

public class RolloutAnalyzer<N> {

	private Graph<N> explorationGraph = new Graph<>();
	private final Map<N, Integer> depths = new HashMap<>();
	private final Map<N, DescriptiveStatistics> currentScoreOfNodes = new HashMap<>();
	private final Map<N, Integer> iterationOfLastRollout = new HashMap<>();
	private final Map<N, Map<N, DescriptiveStatistics>> statsOfChildrenOfNodesAtTimeOfLastRolloutOfNodeWithLessRollouts = new HashMap<>();
	private final Map<N, Integer> iterationOfDecision = new HashMap<>();
	private final Map<N, List<N>> decisionLists = new HashMap<>();
	private int numRollouts = 0;

	@Subscribe
	public void receiveRolloutEvent(final RolloutEvent<N, Double> event) {
		this.explorationGraph.addPath(event.getPath());
		this.numRollouts ++;
		AtomicInteger depth = new AtomicInteger();
		N parent = null;
		for (N node : event.getPath()) {
			this.depths.computeIfAbsent(node, n -> depth.get());
			this.currentScoreOfNodes.computeIfAbsent(node, n -> new DescriptiveStatistics()).addValue(event.getScore());
			this.iterationOfLastRollout.put(node, this.numRollouts);
			depth.getAndIncrement();
			if (parent != null) {
				Set<N> successors = this.explorationGraph.getSuccessors(parent);
				boolean childWithLessRolloutsIsUpdated = this.getChildrenOfNodesInOrderOfTheNumberOfVisits(parent).get(0).equals(node);
				if (childWithLessRolloutsIsUpdated) {
					Map<N, DescriptiveStatistics> mapForParent = this.statsOfChildrenOfNodesAtTimeOfLastRolloutOfNodeWithLessRollouts.computeIfAbsent(parent, n -> new HashMap<>());
					for (N child : successors) {
						mapForParent.put(child, this.currentScoreOfNodes.get(child).copy());
					}
					this.iterationOfDecision.put(parent, this.numRollouts);
				}
				this.decisionLists.computeIfAbsent(parent, n -> new ArrayList<>()).add(node);
			}
			parent = node;
		}
	}

	public List<N> getMostVisistedSubPath(final int length) {
		List<N> path = new ArrayList<>(length);
		path.add(this.explorationGraph.getRoot());
		N current = path.get(0);
		for (int i = 0; i < length; i++) {
			N mostVisitedChild = null;
			long numVisitsOfThatChild = 0;
			for (N child : this.explorationGraph.getSuccessors(current)) {
				long numVisitsOfThisChild = this.currentScoreOfNodes.get(child).getN();
				if (numVisitsOfThisChild > numVisitsOfThatChild) {
					numVisitsOfThatChild = numVisitsOfThisChild;
					mostVisitedChild = child;
				}
			}
			current = mostVisitedChild;
			path.add(current);
		}
		return path;
	}

	public Map<Integer, int[]> getVisitStatsOfMostVisitedChildrenPerDepth(final int depth, final int memoryLength) {
		List<N> mostVisistedPath = this.getMostVisistedSubPath(depth);

		Map<Integer, int[]> stats = new HashMap<>();
		for (int i = 0; i <= depth; i++) {

			/* go back $memoryLength many steps on the path and compute all children up to the current depth in DFS fashion */
			int historySize = Math.min(i, memoryLength);
			N relevantNode = mostVisistedPath.get(i - historySize);
			List<N> descendants = this.enumerateChildrenOfNodeUpToDepth(relevantNode, historySize);
			int n = descendants.size();
			int[] statsForThisDepth = new int[n];
			for (int j = 0; j < n; j++) {
				statsForThisDepth[j] = (int)this.currentScoreOfNodes.get(descendants.get(j)).getN();
			}
			stats.put(i, statsForThisDepth);
		}
		return stats;
	}

	public List<N> getChildrenOfNodesInOrderOfTheNumberOfVisits(final N node) {
		return this.explorationGraph.getSuccessors(node).stream().sorted((s1, s2) -> Long.compare(this.currentScoreOfNodes.get(s1).getN(), this.currentScoreOfNodes.get(s2).getN())).collect(Collectors.toList());
	}

	public Map<Integer, int[]> getLatestRolloutAlongMostVisitedChildrenPerDepth(final int depth, final int memoryLength) {
		List<N> mostVisistedPath = this.getMostVisistedSubPath(depth);

		Map<Integer, int[]> stats = new HashMap<>();
		for (int i = 0; i <= depth; i++) {

			/* go back $memoryLength many steps on the path and compute all children up to the current depth in DFS fashion */
			int historySize = Math.min(i, memoryLength);
			N relevantNode = mostVisistedPath.get(i - historySize);
			List<N> descendants = this.enumerateChildrenOfNodeUpToDepth(relevantNode, historySize);
			int n = descendants.size();
			int[] statsForThisDepth = new int[n];
			for (int j = 0; j < n; j++) {
				statsForThisDepth[j] = this.iterationOfLastRollout.get(descendants.get(j));
			}
			stats.put(i, statsForThisDepth);
		}
		return stats;
	}

	public Map<Integer, Map<N, DescriptiveStatistics>> getChildrenStatisticsAtPointOfDecisionOfMostVisitedPathPerDepth(final int depth) {
		List<N> mostVisistedPath = this.getMostVisistedSubPath(depth);
		Map<Integer, Map<N, DescriptiveStatistics>> stats = new HashMap<>();
		for (int i = 0; i <= depth; i++) {
			N node = mostVisistedPath.get(i);
			Map<N, DescriptiveStatistics> statsOfChildren = this.statsOfChildrenOfNodesAtTimeOfLastRolloutOfNodeWithLessRollouts.get(node);
			stats.put(i, statsOfChildren);
		}
		return stats;
	}

	public List<N> enumerateChildrenOfNodeUpToDepth(final N node, final int depth) {
		if (depth == 0) {
			return Arrays.asList(node);
		}
		List<N> nodes = new ArrayList<>((int)Math.pow(2, depth));
		for (N child : this.explorationGraph.getSuccessors(node)) {
			nodes.addAll(this.enumerateChildrenOfNodeUpToDepth(child, depth - 1));
		}
		return nodes;
	}

	public Map<Integer, DescriptiveStatistics> getVisitStatsPerDepth() {
		Map<Integer, DescriptiveStatistics> stats = new HashMap<>();
		for (N node : this.explorationGraph.getItems()) {
			stats.computeIfAbsent(this.depths.get(node), n -> new DescriptiveStatistics()).addValue(this.currentScoreOfNodes.get(node).getN());
		}
		return stats;
	}

	public Map<N, Integer> getIterationOfDecision() {
		return this.iterationOfDecision;
	}

	public List<N> getDecisionListForNode(final N node) {
		return this.decisionLists.get(node);
	}
}
