package jaicore.ml.dyadranking.optmization;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import jaicore.ml.dyadranking.algorithm.lbfgs.LFBGSOptimizerWrapper;

/**
 * Tests the LFBGS wrapper by letting it optimize polynomial models
 * 
 * @author elppa
 *
 */
@RunWith(Parameterized.class)
public class LFBGSTest {
	
	private LFBGSTestData data;
	
	public LFBGSTest (LFBGSTestData data) {
		this.data = data;
	}
	
	@Test
	public void testOptimiziation () {
		LFBGSOptimizerWrapper wrapper = new LFBGSOptimizerWrapper();
		wrapper.optimize(data.getFunction(), data.getGradient(), new DenseDoubleVector(new double [] {90}));
	}

	@Parameters
	public static List<LFBGSTestData> getTestData() {
		ArrayList<LFBGSTestData> toReturn = new ArrayList<>();
		// x^2
		toReturn.add(LFBGSTestData.polynomialFromCoeffs(new double[] { 1, 1, 1 }, new double[] { 1 }));

		return toReturn;

	}
}
