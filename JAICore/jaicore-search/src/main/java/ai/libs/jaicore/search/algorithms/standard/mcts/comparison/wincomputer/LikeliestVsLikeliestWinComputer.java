package ai.libs.jaicore.search.algorithms.standard.mcts.comparison.wincomputer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.api4.java.datastructure.graph.IPath;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.IWinComputer;

public class LikeliestVsLikeliestWinComputer implements IWinComputer {

	private final BradleyTerryLikelihoodPolicy policy;

	private final Map<IPath, Double> scores = new HashMap<>();

	private final int k = 20;

	public LikeliestVsLikeliestWinComputer(final BradleyTerryLikelihoodPolicy<?, ?> policy) {
		super();
		this.policy = policy;
	}

	@Override
	public void updateWinsOfChildrenBasedOnNewScore(final BTModel nodeModel, final double newScore, final boolean forRightChild) {

		BTModel relevantChildOfUpdated = forRightChild ? nodeModel.getRight() : nodeModel.getLeft();

		/* if this is a leaf node, add the path to the stats */
		if (relevantChildOfUpdated.getLeft() == null && relevantChildOfUpdated.getRight() == null) {
			this.scores.put(relevantChildOfUpdated.getPath(), newScore);
			//			System.out.println("Adding " + relevantChildOfUpdated.getPath().getHead());
		}

		/* now wins if we do not have at least one score on the left and on the right */
		if (nodeModel.getLeft() == null || nodeModel.observedScoresLeft.size() < this.k || nodeModel.getRight() == null || nodeModel.observedScoresRight.size() < this.k) {
			return;
		}

		/* get most likely k paths under left and under right */
		Collection<IPath> bestPathsUnderLeft = this.policy.getMostLikelyPathsUnderNode(nodeModel.getLeft().getNode(), this.k);
		Collection<IPath> bestPathsUnderRight = this.policy.getMostLikelyPathsUnderNode(nodeModel.getRight().getNode(), this.k);
		int winsLeft = 0;
		int winsRight = 0;
		double bestLeftObservation = (double)nodeModel.observedScoresLeft.peekFirst();
		double bestRightObservation = (double)nodeModel.observedScoresRight.peekFirst();
		boolean bestLeftIsAmongLikely = false;
		boolean bestRightIsAmongLikely = false;
		for (IPath left : bestPathsUnderLeft) {
			assert this.scores.containsKey(left) : "Score map with " + this.scores.size() + " entries does not contain entry for " + left.getHead();
			double leftScore = this.scores.get(left);
			if (leftScore == bestLeftObservation) {
				bestLeftIsAmongLikely = true;
			}
			for (IPath right : bestPathsUnderRight) {
				double rightScore = this.scores.get(right);
				if (rightScore == bestRightObservation) {
					bestRightIsAmongLikely = true;
				}
				if (rightScore <= leftScore) {
					winsRight ++;
				}
				if (leftScore <= rightScore) {
					winsLeft ++;
				}
			}
		}
		if (!bestLeftIsAmongLikely) {
			System.err.println("Best left " + bestLeftObservation + " is not among likely! New observation: " + newScore);
		}
		if (!bestRightIsAmongLikely) {
			System.err.println("Best right is not among likely!");
		}
		nodeModel.setWinsLeft(winsLeft);
		nodeModel.setWinsRight(winsRight);
	}
}
