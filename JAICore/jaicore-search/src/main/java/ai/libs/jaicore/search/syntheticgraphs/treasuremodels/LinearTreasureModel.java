package ai.libs.jaicore.search.syntheticgraphs.treasuremodels;

import java.math.BigInteger;

import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.syntheticgraphs.graphmodels.ITransparentTreeNode;

/**
 * Distributes scores according to a simple linear function from the left to the right.
 *
 * @author felix
 *
 */
public class LinearTreasureModel implements ITreasureModel {

	private final boolean asc;

	public LinearTreasureModel() {
		this(true);
	}

	public LinearTreasureModel(final boolean asc) {
		super();
		this.asc = asc;
	}

	@Override
	public Double evaluate(final IPath<ITransparentTreeNode, Integer> path) throws PathEvaluationException, InterruptedException {
		BigInteger numLeafsBefore = path.getHead().getNumberOfLeafsPriorToNodeViaDFS();
		return this.asc ? numLeafsBefore.doubleValue() : path.getRoot().getNumberOfLeafsUnderNode().subtract(numLeafsBefore).doubleValue();
	}

	@Override
	public double getMinimumAchievable() {
		return 0;
	}

}
