package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.MinMaxPriorityQueue;

import ai.libs.jaicore.math.linearalgebra.AffineFunction;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;

/**
 * keeps the k best and n - k random observations
 *
 * @author felix
 */
public class MixedObservationUpdate implements IObservationUpdate {

	private final int k;
	private final int n;
	private final int randomlyChosen;

	private final double epsilon = 1.0;

	public MixedObservationUpdate(final int n, final int k) {
		super();
		this.k = k;
		this.n = n;
		this.randomlyChosen = n - k;
	}

	@Override
	public boolean updateObservations(final BTModel model, final double score, final boolean isForRightChild) {
		MinMaxPriorityQueue<Double> queue = isForRightChild ? model.observedScoresRight : model.observedScoresLeft;
		queue.add(score);
		DescriptiveStatistics leftStats = new DescriptiveStatistics();
		model.observedScoresLeft.forEach(d -> leftStats.addValue((double)d));
		DescriptiveStatistics rightStats = new DescriptiveStatistics();
		model.observedScoresRight.forEach(d -> rightStats.addValue((double)d));
		int numObservationsToConsider = this.n;
		if (leftStats.getN() > 0 && rightStats.getN() > 0) {
			//			double leftMean = leftStats.getMean();
			//			double rightMean = rightStats.getMean();
			//			double smallestMean = Math.min(leftMean, rightMean);
			//			double relativeDeviation = Math.abs(leftMean - rightMean) / smallestMean;
			//			//			System.out.println(leftMean + ", " + rightMean + " -> " + relativeDeviation);
			numObservationsToConsider = Math.max(2, (int)Math.round(new AffineFunction(0, this.n, 100, this.k).apply(model.depth)));
		}

		assert this.checkOrderOfQueue(queue);

		double bestScore = queue.peekFirst();
		boolean elementsRemoved = false;
		while (queue.size() > numObservationsToConsider || queue.peekLast() > (1 + this.epsilon) * bestScore) {
			queue.removeLast();
			elementsRemoved = true;
		}
		return elementsRemoved;
	}

	private boolean checkOrderOfQueue(final MinMaxPriorityQueue<Double> queue) {
		double lastScore = -1 * Double.MAX_VALUE;
		int i = 0;
		double observedMin = Double.MAX_VALUE;
		double observedMax = -1 * Double.MAX_VALUE;
		for (Double score : queue) {
			//			if (score < lastScore) {
			//				System.err.println(i + "-th entry " + score + " is smaller than previous entry " + lastScore);
			//				return false;
			//			}
			if (observedMax < score) {
				observedMax = score;
			}
			if (observedMin > score) {
				observedMin = score;
			}
			lastScore = score;
			i++;
		}
		if (observedMin != queue.peekFirst()) {
			System.err.println("Minimum is not the first element!");
			return false;
		}
		if (observedMax != queue.peekLast()) {
			System.err.println("Maximum is not the first element!");
			return false;
		}
		return true;
	}

}
