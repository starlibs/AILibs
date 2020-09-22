package ai.libs.jaicore.search.algorithms.mdp.mcts.comparison;

public class CombinedGammaFunction implements IGammaFunction {
	private final IGammaFunction shortTermGamma;
	private final IGammaFunction longTermGamma;
	private static final double MID_WEIGHT = 0.8;
	private static final double ZERO_OFFSET = -5;
	private static final double LONG_TERM_BREAK = .1;
	private final double slope;

	public CombinedGammaFunction(final IGammaFunction shortTermGamma, final IGammaFunction longTermGamma) {
		super();
		this.shortTermGamma = shortTermGamma;
		this.longTermGamma = longTermGamma;

		/* first compute target of linear transformation for mid */
		double z = -1 * Math.log(1 / MID_WEIGHT - 1);

		/* second compute the linear transformation */
		this.slope = (ZERO_OFFSET - z) / -.5;
	}

	@Override
	public double getNodeGamma(final int visits, final double nodeProbability, final double relativeDepth) {
		double longTermWeight = this.getLongTermWeightBasedOnProbability(nodeProbability);
		double vLongTermGamma = this.longTermGamma.getNodeGamma(visits, nodeProbability, relativeDepth);
		if (longTermWeight > LONG_TERM_BREAK && vLongTermGamma == 0) {
			return 0;
		}
		double vShortTermGamma = this.shortTermGamma.getNodeGamma(visits, nodeProbability, relativeDepth);
		return vLongTermGamma * longTermWeight +  vShortTermGamma * (1 - longTermWeight);
	}

	public double getLongTermWeightBasedOnProbability(final double nodeProbability) {

		/* compute input for sigmoid */
		double xp = this.slope * nodeProbability + ZERO_OFFSET;

		/* return sigmoid */
		return 1 / (1 + Math.exp(-1 * xp));
	}
}
