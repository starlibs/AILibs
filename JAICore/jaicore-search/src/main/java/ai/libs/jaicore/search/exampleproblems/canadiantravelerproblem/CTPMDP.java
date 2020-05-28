package ai.libs.jaicore.search.exampleproblems.canadiantravelerproblem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.basic.sets.SetUtil;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.search.exampleproblems.lake.ECTPEdgeKnowledge;
import ai.libs.jaicore.search.probleminputs.AMDP;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;

public class CTPMDP extends AMDP<CTPState, Short, Double> {

	private final LabeledGraph<Short, Double> network;

	public CTPMDP(final LabeledGraph<Short, Double> network) {
		super(new CTPState(new ShortArrayList(Arrays.asList((short)0)), new HashMap<>()));
		this.network = network;

		/* now set all edges in the MDP for reachable initial neighbors to known (either free or blocked) */
		for (short succ : network.getSuccessors((short)0)) {
			this.getInitState().getEdgeKnowledge().put(new Pair<>((short)0, succ), Math.random() < .5 ? ECTPEdgeKnowledge.KNOWN_BLOCKED : ECTPEdgeKnowledge.KNOWN_FREE);
		}
	}

	@Override
	public Collection<Short> getApplicableActions(final CTPState state) {
		Collection<Short> applicable = new ArrayList<>();
		for (short nextPos : this.network.getConnected(state.getPosition())) {
			if (state.getCurrentTour().contains(nextPos)) { // avoid that a visited position is visited again (except the start 0)
				if (nextPos != 0 || state.getCurrentTour().size() != this.network.getItems().size()) {
					continue;
				}
			}
			short first = (short)Math.min(state.getPosition(), nextPos);
			short second = (short)Math.max(state.getPosition(), nextPos);
			ECTPEdgeKnowledge edgeKnowledge = state.getEdgeKnowledge().get(new Pair<>(first, second));
			if (edgeKnowledge == null || edgeKnowledge == ECTPEdgeKnowledge.UNKNOWN) {
				throw new IllegalStateException("Being at one end of an edge, we should know wheter or not it is blocked. However, knowledge is: " + edgeKnowledge);
			}
			if (edgeKnowledge == ECTPEdgeKnowledge.KNOWN_FREE) {
				applicable.add(nextPos);
			}
		}
		return applicable;
	}

	@Override
	/**
	 *  being now in "action" as the new state, check for each of the places reachable from there whether the roads are blocked */
	public Map<CTPState, Double> getProb(final CTPState state, final Short action) throws InterruptedException {

		/* there will be one possible successor state for each combination of cases of edges to locations we have NOT VISITED so far */
		List<Pair<Short, Short>> unknownEdges = new ArrayList<>();
		for (short nextPos : this.network.getConnected(action)) {
			if (state.getCurrentTour().contains(nextPos)) { // edges to places already visited are irrelevant
				continue;
			}
			short first = (short)Math.min(action, nextPos);
			short second = (short)Math.max(action, nextPos);
			Pair<Short, Short> edge = new Pair<>(first, second);
			if (!state.getEdgeKnowledge().containsKey(edge)) {
				unknownEdges.add(edge);
			}
		}

		/* insert one successor for each possible knowledge acquisition and assuming uniform probabilities */
		if (unknownEdges.isEmpty()) { // this happens when we move the last point, because we know whether there is snow from the origin to that point
			Map<CTPState, Double> out = new HashMap<>();
			Map<Pair<Short, Short>, ECTPEdgeKnowledge> curKnowledge = state.getEdgeKnowledge();
			ShortList newTour = new ShortArrayList(state.getCurrentTour());
			newTour.add((short)action);
			CTPState succ = new CTPState(newTour, new HashMap<>(curKnowledge));
			out.put(succ, 1.0); // we can then be sure to reach this state
			return out;
		}
		else {
			Collection<List<ECTPEdgeKnowledge>> combos = SetUtil.cartesianProduct(Arrays.asList(ECTPEdgeKnowledge.KNOWN_FREE, ECTPEdgeKnowledge.KNOWN_BLOCKED), unknownEdges.size());
			Map<CTPState, Double> out = new HashMap<>();
			double prob = 1.0 / combos.size();
			ShortList newTour = new ShortArrayList(state.getCurrentTour());
			Map<Pair<Short, Short>, ECTPEdgeKnowledge> curKnowledge = state.getEdgeKnowledge();
			newTour.add((short)action);
			for (List<ECTPEdgeKnowledge> combo : combos) {
				Map<Pair<Short, Short>, ECTPEdgeKnowledge> newKnowledge = new HashMap<>(curKnowledge);
				for (int i = 0; i < unknownEdges.size(); i++) {
					newKnowledge.put(unknownEdges.get(i), combo.get(i));
				}
				CTPState succ = new CTPState(newTour, newKnowledge);
				out.put(succ, prob);
			}
			return out;
		}
	}

	@Override
	public Double getScore(final CTPState state, final Short action, final CTPState successor) {
		short first = (short)Math.min(state.getPosition(), action);
		short second = (short)Math.max(state.getPosition(), action);
		if (this.getApplicableActions(successor).isEmpty()) {
			return Double.MAX_VALUE;
		}
		return this.network.getEdgeLabel(first, second).doubleValue();
	}

	@Override
	public boolean isMaximizing() {
		return false;
	}
}
