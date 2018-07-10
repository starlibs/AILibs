package jaicore.ml;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import jaicore.ml.core.Interval;
import jaicore.ml.intervaltree.ExtendedRandomTree;
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

	public ExtendedRandomTreeTest(RandomTreeParams params) {
		this.params = params;
	}

	@Test
	public void testTree() {
		Instances trainingData = params.getTrainingData();
		ExtendedRandomTree tree = new ExtendedRandomTree();
		try {
			tree.buildClassifier(trainingData);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Entry<Instance, Interval> e = params.getTestData();
		Interval predicted = tree.predictInterval(e.getKey());
		System.out.println("Range-Query: " + e.getKey());
		System.out.println("Predicted: " + predicted + ", Actual " + e.getValue());
	}

	@Parameters
	public static Collection<RandomTreeParams[]> getParameters() {
		return Arrays.asList(new RandomTreeParams[][] {
			{ new RandomTreeParams((x) -> Math.pow(x, 2), (x) -> 2 * x) },
			{ new RandomTreeParams((x) -> Math.sin(x), (x) -> Math.cos(x)) },
			{ new RandomTreeParams((x) -> Math.pow(x, 10), (x) -> 10 * Math.pow(x, 9)) }
			});
	}

	static class RandomTreeParams {
		private static final double lowerBound = 0;

		private static final double upperBound = 10;

		private static final double stepSize = 0.1;

		private static final double gradientDescentStepSize = 0.01;

		private static final double gradTreshold = 0.01;

		private static final int maxRuns = 500;

		private static final int randomStarts = 10;

		Function<Double, Double> grad;

		Function<Double, Double> fun;

		public RandomTreeParams(Function<Double, Double> fun, Function<Double, Double> grad) {
			this.fun = fun;
			this.grad = grad;
		}

		public Instances getTrainingData() {
			List<Instance> instances = new ArrayList<>();
			for (double i = lowerBound; i < upperBound; i += stepSize) {
				Instance instance = new DenseInstance(2);
				instance.setValue(0, i);
				instance.setValue(1, fun.apply(i));
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
		private double getOptima(int plusMinus, Interval xInterval) {
			// strategy: gradient descent pick 10 random start points got into the negative
			// direction until either a local optima has been reached(gradient close enough
			// to 0), or, an upper/lower bound has been reached.
			double[] randomStart = new double[randomStarts];
			for (int i = 0; i < randomStarts; i++) {
				randomStart[i] = Math.random() * (xInterval.getUpperBound() - xInterval.getLowerBound())
						+ xInterval.getLowerBound();
			}
			if (plusMinus == +1)
				return Arrays.stream(randomStart).mapToObj(x -> singleOptimaRun(plusMinus, x, xInterval))
						.max(Double::compare).orElseThrow(() -> new IllegalStateException());
			else
				return Arrays.stream(randomStart).mapToObj(x -> singleOptimaRun(plusMinus, x, xInterval))
						.min(Double::compare).orElseThrow(() -> new IllegalStateException());

		}

		private double singleOptimaRun(int plusMinus, double startX, Interval range) {
			double lower = range.getLowerBound();
			double upper = range.getUpperBound();
			double currentX = startX;
			double currentGrad = grad.apply(currentX);
			double nextX = currentX;
			double nextGrad = currentGrad;
			int runs = 0;
			// System.out.println("Random start point is " + startX + ", seraching for max:"
			// + (plusMinus == +1));
			while (runs < maxRuns && nextRunFitsANegativeUpdate(nextX, lower)
					&& nextRunFitsAPositiveUpdate(nextX, upper) && !gradIsCloseToZero(nextGrad)) {
				currentX = nextX;
				currentGrad = nextGrad;
				int gradientSignum = currentGrad < 0 ? -1 : +1;
				nextX = currentX + (plusMinus * gradientSignum * gradientDescentStepSize);
				nextGrad = grad.apply(currentX);
				runs++;
			}
			// System.out.println("Returning with X:" + currentX + ", grad:" + currentGrad +
			// " f(X):" + fun.apply(currentX));
			// either of the conditions is met
			return fun.apply(currentX);
		}

		private boolean nextRunFitsANegativeUpdate(double nextX, double lowerBound) {
			return nextX > lowerBound + gradientDescentStepSize;
		}

		private boolean nextRunFitsAPositiveUpdate(double nextX, double upperBound) {
			return nextX < upperBound - gradientDescentStepSize;
		}

		private boolean gradIsCloseToZero(double grad) {
			return (grad <= gradTreshold) && (grad >= -gradTreshold);
		}

		private double getMin(Interval xInterval) {
			return getOptima(-1, xInterval);
		}

		private double getMax(Interval xInterval) {
			return getOptima(+1, xInterval);
		}

		public Entry<Instance, Interval> getTestData() {
			double random1 = Math.random() * (upperBound - lowerBound) + lowerBound;
			double random2 = Math.random() * (upperBound - lowerBound) + lowerBound;
			Interval rangeQuery = new Interval(Double.min(random1, random2), Double.max(random1, random2));
			Interval minMaxInterval = new Interval(getMin(rangeQuery), getMax(rangeQuery));
			Instance testInstance = new DenseInstance(2);
			testInstance.setValue(0, rangeQuery.getLowerBound());
			testInstance.setValue(1, rangeQuery.getUpperBound());
			return new AbstractMap.SimpleEntry<Instance, Interval>(testInstance, minMaxInterval);
		}
	}

}
