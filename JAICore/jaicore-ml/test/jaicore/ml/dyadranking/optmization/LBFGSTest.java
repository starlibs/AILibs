package jaicore.ml.dyadranking.optmization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.algorithm.lbfgs.LBFGSOptimizerWrapper;

/**
 * Tests the LFBGS wrapper by letting it minimize polynomials.
 * The polynomials that should be minimized are provided .
 * @author Mirko 
 *
 */
@RunWith(Parameterized.class)
public class LBFGSTest {
	
	private LBFGSTestData data;
	
	public LBFGSTest (LBFGSTestData data) {
		this.data = data;
	}
	
	@Test
	public void testOptimiziation () {
		LBFGSOptimizerWrapper wrapper = new LBFGSOptimizerWrapper();
		Vector result = wrapper.optimize(data.getFunction(), data.getGradient(), new DenseDoubleVector(new double [] {90}));
		System.out.println("LBFGS returned "+ Arrays.toString(result.asArray()));
		Assert.assertArrayEquals(data.getExpectedResult(), result.asArray(), 0.0001);
	}

	@Parameters()
	public static List<LBFGSTestData> getTestData() {
		ArrayList<LBFGSTestData> toReturn = new ArrayList<>();
		//test data: the coeffs describe polynomials the minima are taken from stack overflow
		toReturn.add(LBFGSTestData.polynomialFromCoeffs(new double[] { 500, 3000, 3, 5, 6 }, new double[] { -5.19989}));
		toReturn.add(LBFGSTestData.polynomialFromCoeffs(new double[] { 1, 1, 1 }, new double[] { -0.5 }));
		toReturn.add(LBFGSTestData.polynomialFromCoeffs(new double[] { 1, 2, 3 }, new double[] { -1.0/3.0 }));
		
		return toReturn;

	}
}
