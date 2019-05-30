package jaicore.ml.core.dataset.sampling.inmemory;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;
import org.junit.Test;

import com.google.common.collect.Lists;

import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.AttributeDiscretizationPolicy;
import jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling.DiscretizationHelper;

public class DiscretizationHelperTester {

	private static final int NUMBER_OF_CATEGORIES = 2;

	private DiscretizationHelper<?> discretizationHelper = new DiscretizationHelper<>();

	@Test
	public void testEqualSizePolicyEven() {
		List<Double> values = this.getTestDoublesEvenSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalSizePolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Lists.newArrayList(new Interval(0.0, 5.0), new Interval(6.0, 20.0));
		assertEquals(expectedIntervals, policy.getIntervals());
	}

	@Test
	public void testEqualSizePolicyOdd() {
		List<Double> values = this.getTestDoublesOddSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalSizePolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Lists.newArrayList(new Interval(0.0, 3.0), new Interval(5.0, 20.0));
		assertEquals(expectedIntervals, policy.getIntervals());
	}

	@Test
	public void testEqualLengthPolicyEven() {
		List<Double> values = this.getTestDoublesEvenSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalLengthPolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Lists.newArrayList(new Interval(0.0, 10.0), new Interval(10.0, 20.0));
		assertEquals(expectedIntervals, policy.getIntervals());
	}

	@Test
	public void testEqualLengthPolicyOdd() {
		List<Double> values = this.getTestDoublesOddSize();
		AttributeDiscretizationPolicy policy = this.discretizationHelper.equalLengthPolicy(values, NUMBER_OF_CATEGORIES);
		List<Interval> expectedIntervals = Lists.newArrayList(new Interval(0.0, 10.0), new Interval(10.0, 20.0));
		assertEquals(expectedIntervals, policy.getIntervals());
	}

	private List<Double> getTestDoublesEvenSize() {
		return Lists.newArrayList(0.0, 2.0, 3.0, 5.0, 6.0, 10.0, 15.0, 20.0);
	}

	private List<Double> getTestDoublesOddSize() {
		return Lists.newArrayList(0.0, 2.0, 3.0, 5.0, 6.0, 10.0, 20.0);
	}

}
