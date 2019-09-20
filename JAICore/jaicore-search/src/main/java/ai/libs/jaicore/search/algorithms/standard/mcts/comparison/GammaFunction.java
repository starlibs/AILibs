package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.function.DoubleFunction;
import java.util.function.Function;

import org.apache.commons.math3.analysis.function.Cos;

import ai.libs.jaicore.math.linearalgebra.AffineFunction;
import ai.libs.jaicore.search.algorithms.standard.mcts.comparison.BradleyTerryLikelihoodPolicy.BTModel;

public class GammaFunction implements IGammaFunction {

	private Cos cos = new Cos();

	/* short term exploration definintion */
	private final int shortMinObservationsEnforcedForAnyDecision = 2;
	private final int shortAbsoluteMaxStepsToReachGammaOne = 4;
	private final int shortAbsoluteMaxStepsToReachMaxGamma = 10;
	private final double shortMaxGamma = 5;

	/* long term exploration definintion */
	private final int minObservationsEnforcedForAnyDecision = 5;
	private final int absoluteMaxStepsToReachGammaOne = 100;
	private final int absoluteMinStepsToReachMaxGamma = this.absoluteMaxStepsToReachGammaOne  + 50;
	private final int absoluteMaxStepsToReachMaxGamma = this.absoluteMinStepsToReachMaxGamma + 250;
	private final double maxGamma = 1;
	private final AffineFunction verticalScale = new AffineFunction(0, 0, 1, this.maxGamma);

	/* this function describe in general the exploration behavior (in the unit interval) */
	private final DoubleFunction<Double> exploitationShape = x -> {
		if (x < 0 || x > 1) {
			throw new IllegalArgumentException();
		}
		double val = 0.5 * (this.cos.value(x * Math.PI) + 1); // this function determines the course of the exponent gamma
		if (val > 1 || val < 0) {
			throw new IllegalStateException("shape range must be within unit interval!");
		}
		return 1 - val;
	};

	private final Function<BTModel, Integer> observationsToReachMaxGamma = n -> {
		int absoluteDepth = n.depth;

		if (n.maxObservedDepthUnderNode == -1) {
			throw new IllegalArgumentException("Cannot compute gamma for node before a depth has been observed under it.");
		}
		double relativeDepth = absoluteDepth * 1.0 / (absoluteDepth + n.maxObservedDepthUnderNode);
		if (Double.valueOf(relativeDepth).equals(Double.NaN)) {
			throw new IllegalStateException("Could not compute relative depth for node " + n + " with absolute depth " + absoluteDepth + " and max observed depth " + n.maxObservedDepthUnderNode + " under it.");
		}
		int val = Math.max(this.absoluteMinStepsToReachMaxGamma, (int)Math.round(new AffineFunction(0, this.absoluteMaxStepsToReachMaxGamma, 1.0, this.absoluteMinStepsToReachMaxGamma).apply(1 - relativeDepth)));
		//		int val = (int)Math.round((this.absoluteMaxStepsToReachMaxGamma - this.absoluteMinStepsToReachMaxGamma) * 1.0 / (1 + absoluteDepth)) + this.absoluteMinStepsToReachMaxGamma;
		//		this.logger.debug("Determined numbers of observations to reach max for node in depth {} with value {} = round(({} - {})/(2^{})) + {}", absoluteDepth, val, this.absoluteMaxStepsToReachMaxGamma, this.absoluteMinStepsToReachMaxGamma, absoluteDepth + 1, this.absoluteMinStepsToReachMaxGamma);
		//		return this.absoluteMaxStepsToReachMaxGamma;
		//		System.out.println(absoluteDepth + " ->  " + val);
		return val;
	};

	private double getExploitationBasedOnlyOnVisits(final int leftVisits, final int rightVisits, final int minTreshold, final int visitsToReachOne, final int visitsToReachMax, final double max) {
		int visits = leftVisits + rightVisits;
		//		this.logger.debug("Required iterations for max gamma of this node in depth {} is {}", n.depth, iterationsToMaxForThisNode);
		if (Math.min(leftVisits, rightVisits) <= this.minObservationsEnforcedForAnyDecision) {
			return 0.0;
		}
		double g;
		if (visits > visitsToReachOne) {
			g = Math.min(max, new AffineFunction(visitsToReachOne, 1, visitsToReachMax, max).apply(visits));
		}
		else {
			double scaledValue = (visits - minTreshold) * 1.0 / (visitsToReachOne - minTreshold);
			if (scaledValue < 0 || scaledValue > 1) {
				throw new IllegalStateException();
			}
			g = this.exploitationShape.apply(scaledValue);
			if (g < 0 || g > 1) {
				throw new IllegalStateException();
			}
		}
		//		if (n.depth > 50) {
		//			System.out.println(g + " for " + n.visits + " visits, requiring " + iterationsToReachGammaOneForThisNode + "/" + iterationsToReachMaxGammaForThisNode);
		//		}
		if (g < 0 || g > max) {
			throw new IllegalStateException();
		}
		return g;
	}

	private double getLongTermGamma(final BTModel n) {
		int iterationsToReachMaxGammaForThisNode = this.observationsToReachMaxGamma.apply(n);
		int iterationsToReachGammaOneForThisNode = (int)Math.round(this.absoluteMaxStepsToReachGammaOne * 1.0 / this.absoluteMaxStepsToReachMaxGamma * iterationsToReachMaxGammaForThisNode);
		return this.getExploitationBasedOnlyOnVisits(n.left.visits, n.right.visits, this.minObservationsEnforcedForAnyDecision, iterationsToReachGammaOneForThisNode, iterationsToReachMaxGammaForThisNode, this.maxGamma);
	}

	private double getShortTermGamma(final BTModel n) {
		int iterationsToReachGammaOneForThisNode = this.shortAbsoluteMaxStepsToReachGammaOne;
		int iterationsToReachMaxGammaForThisNode = this.shortAbsoluteMaxStepsToReachMaxGamma;
		return this.getExploitationBasedOnlyOnVisits(n.left.visits, n.right.visits, this.shortMinObservationsEnforcedForAnyDecision, iterationsToReachGammaOneForThisNode, iterationsToReachMaxGammaForThisNode, this.shortMaxGamma);
	}

	@Override
	public Double apply(final BTModel n) {
		double probability = n.getPathProbability();
		//		System.out.println(probability);
		double gammaLong = this.getLongTermGamma(n);
		double gammaShort = this.getShortTermGamma(n);
		double gamma = gammaLong * probability + gammaShort * (1-probability);
		//		System.out.println(n.depth + " (" + n.visits + " visits): " + gamma + " = " + gammaLong + " * " + probability + " + "  + gammaShort + " * " + (1-probability));
		return gamma;
	}
}
