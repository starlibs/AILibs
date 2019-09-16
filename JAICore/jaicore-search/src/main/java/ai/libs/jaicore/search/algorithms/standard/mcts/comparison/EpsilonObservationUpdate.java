package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;

public class EpsilonObservationUpdate implements IObservationUpdate {

	@Override
	public boolean updateObservations(final BTModel model, final double score, final boolean isForRightChild) {
		boolean observationsRemoved = false;
		double epsilon = .2;//BradleyTerryLikelihoodPolicy.this.epsilonForDecisions.apply(this.node);
		//		BradleyTerryLikelihoodPolicy.this.logger.debug("Adding observation {} to {} child. Epsilon for this node is {}", score, isForRightChild ? "right" : "left", epsilon);
		if (isForRightChild) {
			model.observedScoresRight.add(score);
			double threshold = (double)model.observedScoresRight.peekFirst() * (1 + epsilon);
			while ((double)model.observedScoresRight.peekLast() > threshold) {
				double removed = (double)model.observedScoresRight.removeLast();
				//				BradleyTerryLikelihoodPolicy.this.logger.debug("Removed worst observation {}. Remaining scores: {}", removed, this.observedScoresRight);
				observationsRemoved = true;
			}
		}
		else {
			model.observedScoresLeft.add(score);
			double threshold = (double)model.observedScoresLeft.peekFirst() * (1 + epsilon);
			while ((double)model.observedScoresLeft.peekLast() > threshold) {
				double removed = (double)model.observedScoresLeft.removeLast();
				//				BradleyTerryLikelihoodPolicy.this.logger.debug("Removed worst observation {}. Remaining scores: {}", removed, this.observedScoresLeft);
				observationsRemoved = true;
			}
		}
		return observationsRemoved;
	}
}
