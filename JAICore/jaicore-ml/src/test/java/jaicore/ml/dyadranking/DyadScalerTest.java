package jaicore.ml.dyadranking;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.upb.isys.linearalgebra.DenseDoubleVector;
import de.upb.isys.linearalgebra.Vector;
import jaicore.ml.dyadranking.dataset.DyadRankingDataset;
import jaicore.ml.dyadranking.dataset.DyadRankingInstance;
import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;
import jaicore.ml.dyadranking.util.AbstractDyadScaler;
import jaicore.ml.dyadranking.util.DyadStandardScaler;
import jaicore.ml.dyadranking.util.DyadUnitIntervalScaler;

/**
 * Tests our basic scalers.
 * @author Mirko Jürgens
 *
 */
public class DyadScalerTest {

	private double[] expectedStdResult;
	
	private double[] expectedUnitResult;
	
	private double[] expectedMinMaxResult;


	private DyadRankingDataset testingSet;

	public void setupDataset() {
		Vector instance = new DenseDoubleVector(new double[] { 1.0d});
		Vector alternative = new DenseDoubleVector(new double[] { 1.0d});
		Dyad dOne = new Dyad(instance, alternative);
		instance = new DenseDoubleVector(new double[] {2.0d});
		alternative = new DenseDoubleVector(new double[] {2.0d});
		Dyad dTwo = new Dyad(instance, alternative);
		instance = new DenseDoubleVector(new double[] {3.0d});
		alternative = new DenseDoubleVector(new double[] {3.0d});
		Dyad dThree = new Dyad(instance, alternative);
		instance = new DenseDoubleVector(new double[] {10.0d});
		alternative = new DenseDoubleVector(new double[] {10.0d});
		Dyad dFour = new Dyad(instance, alternative);
		DyadRankingInstance ranking = new DyadRankingInstance(Arrays.asList(dOne, dTwo, dThree, dFour));
		List<IDyadRankingInstance> allRankings = Arrays.asList(ranking);
		testingSet = new DyadRankingDataset(allRankings);
	}

	@Before
	public void setupExpectedResults() {
		// standardization
		//derived by wolfram alpha
		double mean = 4.0;
		double stdDev = 4.0825;
		double stdOne = (1.0 - mean) / stdDev;
		double stdTwo = (2.0 - mean) / stdDev;
		double stdThree = (3.0 - mean) / stdDev;
		double stdFour = (10.0 - mean) / stdDev;
		expectedStdResult = new double[] { stdOne, stdTwo, stdThree, stdFour };
		
		// unit interval scaler
		double lengthOfVector = Math.sqrt(1 + 4 + 9 + 100);
		double unitOne = (1.0/ lengthOfVector);
		double unitTwo =  (2.0/ lengthOfVector);
		double unitThree =  (3.0/ lengthOfVector);
		double unitFour =  (10.0/ lengthOfVector);
		expectedUnitResult = new double [] {unitOne, unitTwo, unitThree, unitFour};
		
		// min max scaler
		double min = 1.0d;
		double max = 10.0d;
		double minmaxDiff = max - min;
		//would be a division b 0
		double minmaxOne = 	(1.0d - min)/ minmaxDiff;
		double minmaxTwo =  (2.0d - min) / minmaxDiff;
		double minmaxThree =  (3.0d - min) / minmaxDiff;
		double minmaxFour =  (10.0d - min) / minmaxDiff;
		expectedMinMaxResult = new double[] {minmaxOne, minmaxTwo, minmaxThree, minmaxFour};
	}

	@Test
	public void testStandardScaler() {
		setupDataset();
		System.out.println("Testing DyadStandardScaler...");
		AbstractDyadScaler stdScaler = new DyadStandardScaler();
		stdScaler.fit(testingSet);
		System.out.println("Testing transform instances...");
		stdScaler.transformInstances(testingSet);
		for (int i = 0; i < 4 ; i++) {
			Assert.assertEquals(expectedStdResult[i], testingSet.get(0).getDyadAtPosition(i).getInstance().getValue(0), 0.0001);
		}
		System.out.println("Testing transform alternatives...");
		stdScaler.transformAlternatives(testingSet);
		for (int i = 0; i < 4 ; i++) {
			Assert.assertEquals(expectedStdResult[i], testingSet.get(0).getDyadAtPosition(i).getAlternative().getValue(0), 0.0001);
		}
	}

	
	@Test
	public void testUnitIntervalScaler() {
		setupDataset();
		System.out.println("Testing UnitIntervalScaler...");
		AbstractDyadScaler stdScaler = new DyadUnitIntervalScaler();
		stdScaler.fit(testingSet);
		System.out.println("Testing transform instances...");
		stdScaler.transformInstances(testingSet);
		for (int i = 0; i < 4 ; i++) {
			Assert.assertEquals(expectedUnitResult[i], testingSet.get(0).getDyadAtPosition(i).getInstance().getValue(0), 0.0001);
		}
		System.out.println("Testing transform alternatives...");
		stdScaler.transformAlternatives(testingSet);
		for (int i = 0; i < 4 ; i++) {
			Assert.assertEquals(expectedUnitResult[i], testingSet.get(0).getDyadAtPosition(i).getAlternative().getValue(0), 0.0001);
		}
	}
	
	@Test
	public void testMinMaxScaler() {
		setupDataset();
		System.out.println("Testing MinMaxScaler...");
		AbstractDyadScaler stdScaler = new DyadUnitIntervalScaler();
		stdScaler.fit(testingSet);
		System.out.println("Testing transform instances...");
		stdScaler.transformInstances(testingSet);
		for (int i = 0; i < 4 ; i++) {
			Assert.assertEquals(expectedMinMaxResult[i], testingSet.get(0).getDyadAtPosition(i).getInstance().getValue(0), 0.0001);
		}
		System.out.println("Testing transform alternatives...");
		stdScaler.transformAlternatives(testingSet);
		for (int i = 0; i < 4 ; i++) {
			Assert.assertEquals(expectedMinMaxResult[i], testingSet.get(0).getDyadAtPosition(i).getAlternative().getValue(0), 0.0001);
		}
	}
}
