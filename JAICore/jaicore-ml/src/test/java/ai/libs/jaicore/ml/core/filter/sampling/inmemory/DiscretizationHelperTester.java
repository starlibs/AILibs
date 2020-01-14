package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.junit.Test;

import com.google.common.collect.Lists;

import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.AttributeDiscretizationPolicy;
import ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling.DiscretizationHelper;

public class DiscretizationHelperTester {

	private static final int NUMBER_OF_CATEGORIES = 2;

	private DiscretizationHelper discretizationHelper = new DiscretizationHelper();

	public static void checkIntervalEqualness(final Interval i1, final Interval i2) {
		assertEquals(i1.getInf(), i2.getInf(), .001);
		assertEquals(i1.getSup(), i2.getSup(), .001);
	}

	public static void checkIntervalListEqualness(final List<Interval> l1, final List<Interval> l2) {
		int n = l1.size();
		assertEquals(n, l2.size());
		for (int i = 0; i < n; i++) {
			checkIntervalEqualness(l1.get(i), l2.get(i));
		}
	}

	@Test
	public void testEqualSizePolicyEven() {
		List<Double> values = this.getTestDoublesEvenSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalSizePolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Arrays.asList(new Interval(0.0, 5.0), new Interval(6.0, 20.0));
		List<Interval> computedIntervals = policy.getIntervals();
		checkIntervalListEqualness(expectedIntervals, computedIntervals);

	}

	@Test
	public void testEqualSizePolicyOdd() {
		List<Double> values = this.getTestDoublesOddSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalSizePolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Lists.newArrayList(new Interval(0.0, 3.0), new Interval(5.0, 20.0));
		checkIntervalListEqualness(expectedIntervals, policy.getIntervals());
	}

	@Test
	public void testEqualLengthPolicyEven() {
		List<Double> values = this.getTestDoublesEvenSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalLengthPolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Lists.newArrayList(new Interval(0.0, 10.0), new Interval(10.0, 20.0));
		checkIntervalListEqualness(expectedIntervals, policy.getIntervals());
	}

	@Test
	public void testEqualLengthPolicyOdd() {
		List<Double> values = this.getTestDoublesOddSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalLengthPolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Lists.newArrayList(new Interval(0.0, 10.0), new Interval(10.0, 20.0));
		checkIntervalListEqualness(expectedIntervals, policy.getIntervals());
	}

	private List<Double> getTestDoublesEvenSize() {
		return Lists.newArrayList(0.0, 2.0, 3.0, 5.0, 6.0, 10.0, 15.0, 20.0);
	}

	private List<Double> getTestDoublesOddSize() {
		return Lists.newArrayList(0.0, 2.0, 3.0, 5.0, 6.0, 10.0, 20.0);
	}

}
