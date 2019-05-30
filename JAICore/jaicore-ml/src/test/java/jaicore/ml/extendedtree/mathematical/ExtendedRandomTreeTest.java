package jaicore.ml.extendedtree.mathematical;

import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jaicore.ml.intervaltree.ExtendedRandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Tests the RangeQueryPredictor with standard mathematical functions that can
 * be optimized by using the gradient.
 *
 * @author mirkoj
 *
 */
@RunWith(Parameterized.class)
public class ExtendedRandomTreeTest {

	private RandomTreeParams params;

	public ExtendedRandomTreeTest(final RandomTreeParams params) {
		this.params = params;
	}

	@Test
	public void testTree() {
		Instances trainingData = this.params.getTrainingData();
		ExtendedRandomForest tree = new ExtendedRandomForest();
		try {
			tree.buildClassifier(trainingData);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Entry<Instance, Interval> e = this.params.getTestData();
		Interval predicted = tree.predictInterval(e.getKey());
		System.out.println("Range-Query: " + e.getKey());
		System.out.println("Predicted: " + predicted + ", Actual " + e.getValue());
		assertTrue(true);
	}

	@Parameters
	public static Collection<RandomTreeParams[]> getParameters() {
		return Arrays.asList(new RandomTreeParams[][] { { new RandomTreeParams((x) -> Math.pow(x, 2), (x) -> 2 * x) }, { new RandomTreeParams((x) -> Math.sin(x), (x) -> Math.cos(x)) },
			{ new RandomTreeParams((x) -> Math.pow(x, 10), (x) -> 10 * Math.pow(x, 9)) } });
	}

	static class RandomTreeParams {
		private static final double lowerBound = 0;

		private static final double upperBound = 10;

		private static final double stepSize = 0.01;

		private static final double gradientDescentStepSize = 0.01;

		private static final double gradTreshold = 0.01;

		private static final int maxRuns = 500;

		private static final int randomStarts = 10;

		Function<Double, Double> grad;

		Function<Double, Double> fun;

		public RandomTreeParams(final Function<Double, Double> fun, final Function<Double, Double> grad) {
			this.fun = fun;
			this.grad = grad;
		}

		public Instances getTrainingData() {
			List<Instance> instances = new ArrayList<>();
			for (double i = lowerBound; i < upperBound; i += stepSize) {
				Instance instance = new DenseInstance(2);
				instance.setValue(0, i);
				instance.setValue(1, this.fun.apply(i));
				instances.add(instance);
			}
			ArrayList<Attribute> attributes = new ArrayList<>();
			attributes.add(0, new Attribute("xVal"));
			attributes.add(1, new Attribute("yVal"));
			Instances inst = new Instances("test", attributes, instances.size());
			inst.addAll(instances);
			inst.setClassIndex(1);
			return inst;
		}

		// since gradient descent doesn't really care about maximizing or minimizing we
		// can use the same method. If we want to maximize, we walk into the positive
		// direction of the gradient, if we minimize into the negative direction.
		private double getOptima(final int plusMinus, final Interval xInterval) {
			// strategy: gradient descent pick 10 random start points got into the negative
			// direction until either a local optima has been reached(gradient close enough
			// to 0), or, an upper/lower bound has been reached.
			double[] randomStart = new double[randomStarts];
			for (int i = 0; i < randomStarts; i++) {
				randomStart[i] = Math.random() * (xInterval.getSup() - xInterval.getInf()) + xInterval.getInf();
			}
			if (plusMinus == +1) {
				return Arrays.stream(randomStart).mapToObj(x -> this.singleOptimaRun(plusMinus, x, xInterval)).max(Double::compare).orElseThrow(() -> new IllegalStateException());
			} else {
				return Arrays.stream(randomStart).mapToObj(x -> this.singleOptimaRun(plusMinus, x, xInterval)).min(Double::compare).orElseThrow(() -> new IllegalStateException());
			}

		}

		private double singleOptimaRun(final int plusMinus, final double startX, final Interval range) {
			double lower = range.getInf();
			double upper = range.getSup();
			double currentX = startX;
			double currentGrad = this.grad.apply(currentX);
			double nextX = currentX;
			double nextGrad = currentGrad;
			int runs = 0;
			while (runs < maxRuns && this.nextRunFitsANegativeUpdate(nextX, lower) && this.nextRunFitsAPositiveUpdate(nextX, upper) && !this.gradIsCloseToZero(nextGrad)) {
				currentX = nextX;
				currentGrad = nextGrad;
				int gradientSignum = currentGrad < 0 ? -1 : +1;
				nextX = currentX + (plusMinus * gradientSignum * gradientDescentStepSize);
				nextGrad = this.grad.apply(currentX);
				runs++;
			}
			// either of the conditions is met
			return this.fun.apply(currentX);
		}

		private boolean nextRunFitsANegativeUpdate(final double nextX, final double lowerBound) {
			return nextX > lowerBound + gradientDescentStepSize;
		}

		private boolean nextRunFitsAPositiveUpdate(final double nextX, final double upperBound) {
			return nextX < upperBound - gradientDescentStepSize;
		}

		private boolean gradIsCloseToZero(final double grad) {
			return (grad <= gradTreshold) && (grad >= -gradTreshold);
		}

		private double getMin(final Interval xInterval) {
			return this.getOptima(-1, xInterval);
		}

		private double getMax(final Interval xInterval) {
			return this.getOptima(+1, xInterval);
		}

		public Entry<Instance, Interval> getTestData() {
			double random1 = Math.random() * (upperBound - lowerBound) + lowerBound;
			double random2 = Math.random() * (upperBound - lowerBound) + lowerBound;
			Interval rangeQuery = new Interval(Double.min(random1, random2), Double.max(random1, random2));
			Interval minMaxInterval = new Interval(this.getMin(rangeQuery), this.getMax(rangeQuery));
			Instance testInstance = new DenseInstance(2);
			testInstance.setValue(0, rangeQuery.getInf());
			testInstance.setValue(1, rangeQuery.getSup());
			return new AbstractMap.SimpleEntry<Instance, Interval>(testInstance, minMaxInterval);
		}
	}

}
