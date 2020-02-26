package ai.libs.jaicore.search.algorithms.standard.mcts.uuct;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.api4.java.datastructure.graph.ILabeledPath;

import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class UUCBPolicy<N, A> implements IPathUpdatablePolicy<N, A, Double> {
	private static final double ALPHA = 3;
	private final IUCBUtilityFunction utilityFunction;
	private final double a;
	private final double b;
	private final double q;
	private final Map<N, DoubleList> observations = new HashMap<>();
	private int t = 0;

	public UUCBPolicy(final IUCBUtilityFunction utilityFunction) {
		super();
		this.utilityFunction = utilityFunction;
		this.a = utilityFunction.getA();
		this.b = utilityFunction.getB();
		this.q = utilityFunction.getQ();
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {

		/* compute score of each successor (eq 16 in paper and 15 in extended) */
		double bestScore = Double.MAX_VALUE * -1;
		A bestAction = null;
		//		System.out.println("Compare arms: ");
		for (Entry<A, N> succ : actionsWithSuccessors.entrySet()) {
			DoubleList observationsOfChild = this.observations.get(succ.getValue());
			double utility = this.utilityFunction.getUtility(observationsOfChild);
			double phiInverse = this.phiInverse((ALPHA * Math.log(this.t)) / observationsOfChild.size());
			//			System.out.println("utility of " + succ.getKey() + ": " + utility);
			double score = utility + phiInverse;
			//			System.out.println(score + " vs " + bestScore);
			if (score > bestScore) {
				bestScore = score;
				bestAction = succ.getKey();
			}
		}
		return bestAction;
	}

	private double phiInverse(final double x) {
		return Math.max(2 * this.b * Math.sqrt(x / this.a), 2 * this.b * Math.pow(x / this.a, this.q / 2));
	}

	@Override
	public void updatePath(final ILabeledPath<N, A> path, final Double playoutScore, final int lengthOfPlayoutPath) {
		path.getNodes().forEach(n -> this.observations.computeIfAbsent(n, node -> new DoubleArrayList()).add((double) playoutScore));
		this.t++;
	}
}
