package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.function.DoubleFunction;

public class CosLinGammaFunction implements IGammaFunction {

	private final double maxGamma;
	private final int visitsToReachOne;
	private final int initialMinThreshold;
	private final int absoluteMinThreshold;

	public CosLinGammaFunction(final double maxGamma, final int visitsToReachOne, final int initialMinThreshold, final int absoluteMinThreshold) {
		super();
		this.maxGamma = maxGamma;
		this.visitsToReachOne = visitsToReachOne;
		this.initialMinThreshold = initialMinThreshold;
		this.absoluteMinThreshold = absoluteMinThreshold;
	}

	/* this function describe in general the exploration behavior (in the unit interval) */
	private final DoubleFunction<Double> exploitationShape = x -> {
		if (x < 0 || x > 1) {
			throw new IllegalArgumentException();
		}
		double val = 0.5 * (Math.cos(x * Math.PI) + 1);
		if (val > 1 || val < 0) {
			throw new IllegalStateException("shape range must be within unit interval!");
		}
		return 1 - val;
	};

	/**
	 * This function computes a mapping into the sigmoid where initialMinThreshold is mapped to 1 and absoluteMinThreshold -1
	 *
	 * @param relativeDepth
	 * @return
	 */
	public int getMinRequiredVisits(final double relativeDepth) {
		final double certaintyBound = 5;
		final double maxRelativeDepthForMinMinThreshold = 0.8;
		double slope = (2 * certaintyBound) / maxRelativeDepthForMinMinThreshold;
		double factor = 1 - 1 / (1 + Math.exp(-1 * (slope * relativeDepth - certaintyBound)));
		double min = factor * (this.initialMinThreshold - this.absoluteMinThreshold) + this.absoluteMinThreshold;
		return (int)Math.round(min);
	}

	@Override
	public double getNodeGamma(final int visits, final double nodeProbability, final double relativeDepth) {
		double g;
		int minThreshold = this.getMinRequiredVisits(relativeDepth);
		if (visits <= minThreshold) {
			return 0.0;
		}
		if (visits > this.visitsToReachOne) {
			g = Math.min(this.maxGamma, Math.pow((double)visits - this.visitsToReachOne, 1.0/3));
		}
		else {
			double scaledValue = (visits - minThreshold) * 1.0 / (this.visitsToReachOne - minThreshold);
			if (scaledValue < 0 || scaledValue > 1) {
				throw new IllegalStateException("Computed intermediate gamma value " + scaledValue);
			}
			g = this.exploitationShape.apply(scaledValue);
			if (g < 0 || g > 1) {
				throw new IllegalStateException();
			}
		}
		if (g < 0 || g > this.maxGamma) {
			throw new IllegalStateException();
		}
		return g;
	}
}
