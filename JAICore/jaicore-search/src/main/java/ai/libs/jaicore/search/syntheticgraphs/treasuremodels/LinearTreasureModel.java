package ai.libs.jaicore.search.syntheticgraphs.treasuremodels;

import java.math.BigInteger;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

public class LinearTreasureModel implements ITreasureModel {

	private final int d;
	private final int b;

	public LinearTreasureModel(final int d, final int b) {
		super();
		this.d = d;
		this.b = b;
	}

	@Override
	public Double evaluate(final IPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		BigInteger sum = BigInteger.ZERO;
		for (int i = this.d - 1; i >= 0; i--) {
			BigInteger factor = BigInteger.valueOf(this.b).pow(i).multiply(BigInteger.valueOf(path.getArcs().get(this.d - i - 1)));
			sum = sum.add(factor);
		}
		while (sum.toString().length() > 20) {
			sum = sum.divide(BigInteger.TEN);
		}
		return sum.doubleValue();
	}

	@Override
	public double getMinimumAchievable() {
		return 0;
	}

}
