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
import jaicore.ml.dyadranking.util.DyadMinMaxScaler;
import jaicore.ml.dyadranking.util.DyadStandardScaler;
import jaicore.ml.dyadranking.util.DyadUnitIntervalScaler;

/**
 * Tests our basic scalers.
 * 
 * @author Mirko Jürgens
 *
 */
public class DyadScalerTest {

	private double[] expectedStdResultX;

	private double[] expectedUnitResultX;

	private double[] expectedMinMaxResultX;

	private double[] expectedStdResultY;

	private double[] expectedUnitResultY;

	private double[] expectedMinMaxResultY;

	private DyadRankingDataset testingSet;

	public void setupDataset() {
		Vector instance = new DenseDoubleVector(new double[] { 1.0d });
		Vector alternative = new DenseDoubleVector(new double[] { 4.0d });
		Dyad dOne = new Dyad(instance, alternative);
		instance = new DenseDoubleVector(new double[] { 2.0d });
		alternative = new DenseDoubleVector(new double[] { 4.0d });
		Dyad dTwo = new Dyad(instance, alternative);
		instance = new DenseDoubleVector(new double[] { 3.0d });
		alternative = new DenseDoubleVector(new double[] { 4.0d });
		Dyad dThree = new Dyad(instance, alternative);
		instance = new DenseDoubleVector(new double[] { 10.0d });
		alternative = new DenseDoubleVector(new double[] { 4.0d });
		Dyad dFour = new Dyad(instance, alternative);
		DyadRankingInstance ranking = new DyadRankingInstance(Arrays.asList(dOne, dTwo, dThree, dFour));
		List<IDyadRankingInstance> allRankings = Arrays.asList(ranking);
		testingSet = new DyadRankingDataset(allRankings);
	}

	@Before
	public void setupExpectedResults() {
		// standardization
		// derived by wolfram alpha
		double mean = 4.0;
		double stdDev = 4.0825;
		double stdOne = (1.0 - mean) / stdDev;
		double stdTwo = (2.0 - mean) / stdDev;
		double stdThree = (3.0 - mean) / stdDev;
		double stdFour = (10.0 - mean) / stdDev;
		double stdAlt = 0.0d;
		expectedStdResultX = new double[] { stdOne, stdTwo, stdThree, stdFour };
		expectedStdResultY = new double[] { stdAlt, stdAlt, stdAlt, stdAlt };

		// unit interval scaler
		double lengthOfVector = Math.sqrt(1 + 4 + 9 + 100);
		double lengthOfAltVector = Math.sqrt(16 + 16 + 16 + 16);
		double unitOne = (1.0 / lengthOfVector);
		double unitTwo = (2.0 / lengthOfVector);
		double unitThree = (3.0 / lengthOfVector);
		double unitFour = (10.0 / lengthOfVector);
		double unitAlt = (4.0 / lengthOfAltVector);
		expectedUnitResultX = new double[] { unitOne, unitTwo, unitThree, unitFour };
		expectedUnitResultY = new double[] { unitAlt, unitAlt, unitAlt, unitAlt };

		// min max scaler
		double min = 1.0d;
		double max = 10.0d;
		double minmaxDiff = max - min;
		// would be a division b 0
		double minmaxOne = (1.0d - min) / minmaxDiff;
		double minmaxTwo = (2.0d - min) / minmaxDiff;
		double minmaxThree = (3.0d - min) / minmaxDiff;
		double minmaxFour = (10.0d - min) / minmaxDiff;
		double minmaxAlt = 0.0d;
		expectedMinMaxResultX = new double[] { minmaxOne, minmaxTwo, minmaxThree, minmaxFour };
		expectedMinMaxResultY = new double[] { minmaxAlt, minmaxAlt, minmaxAlt, minmaxAlt };
	}

	@Test
	public void testStandardScaler() {
		setupDataset();
		System.out.println("Testing DyadStandardScaler...");
		AbstractDyadScaler stdScaler = new DyadStandardScaler();
		stdScaler.fit(testingSet);
		System.out.println("Testing transform instances...");
		stdScaler.transformInstances(testingSet);
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(expectedStdResultX[i], testingSet.get(0).getDyadAtPosition(i).getInstance().getValue(0),
					0.0001);
		}
		System.out.println("Testing transform alternatives...");
		stdScaler.transformAlternatives(testingSet);
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(expectedStdResultY[i],
					testingSet.get(0).getDyadAtPosition(i).getAlternative().getValue(0), 0.0001);
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
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(expectedUnitResultX[i],
					testingSet.get(0).getDyadAtPosition(i).getInstance().getValue(0), 0.0001);
		}
		System.out.println("Testing transform alternatives...");
		stdScaler.transformAlternatives(testingSet);
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(expectedUnitResultY[i],
					testingSet.get(0).getDyadAtPosition(i).getAlternative().getValue(0), 0.0001);
		}
	}

	@Test
	public void testMinMaxScaler() {
		setupDataset();
		System.out.println("Testing MinMaxScaler...");
		AbstractDyadScaler stdScaler = new DyadMinMaxScaler();
		stdScaler.fit(testingSet);
		System.out.println("Testing transform instances...");
		stdScaler.transformInstances(testingSet);
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(expectedMinMaxResultX[i],
					testingSet.get(0).getDyadAtPosition(i).getInstance().getValue(0), 0.0001);
		}
		System.out.println("Testing transform alternatives...");
		stdScaler.transformAlternatives(testingSet);
		for (int i = 0; i < 4; i++) {
			Assert.assertEquals(expectedMinMaxResultY[i],
					testingSet.get(0).getDyadAtPosition(i).getAlternative().getValue(0), 0.0001);
		}
	}
}
