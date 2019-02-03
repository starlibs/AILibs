package jaicore.ml.core.evaluation.measure.mulitlabel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jaicore.ml.core.evaluation.measure.multilabel.InverseF1MacroAverageLMultilabelMeasure;
import meka.core.Metrics;

/**
 * Test if F1 macro averaged by label is computed correctly.
 * 
 * @author Helena Graf
 *
 */
public class F1MacroAvgLTest {

	@Test
	public void testComputation() {
		double [] actual1 = {1, 0, 1, 0, 1, 0, 0, 1, 1};
		double [] expected1 = {1, 0, 1, 0, 1, 0, 1, 0, 1};
		double [] actual2 = {1, 0, 0, 0, 0, 0, 1, 1, 1};
		double [] expected2 = {0, 0, 0, 0, 0, 0, 1, 1, 1};
		double [] actual3 = {1, 1, 0, 0, 1, 0, 0, 1, 1};
		double [] expected3 = {1, 1, 0, 0, 1, 1, 0, 0, 1};
		
		List<double[]> actuals = Arrays.asList(actual1, actual2, actual3);
		List<double[]> expecteds = Arrays.asList(expected1, expected2, expected3);
		
		InverseF1MacroAverageLMultilabelMeasure measure = new InverseF1MacroAverageLMultilabelMeasure();
		double actualResult = measure.calculateAvgMeasure(actuals, expecteds);
		
		int [][] actualsMatrix = new int [3][];
		int [][] expectedsMatrix = new int [3][];
		
		for (int i = 0; i < 3; i++) {
			actualsMatrix[i] = new int[actuals.get(i).length];
			expectedsMatrix[i] = new int [expecteds.get(i).length];
			for (int j = 0; j < actual1.length; j++) {
				actualsMatrix[i][j] = (int) actuals.get(i)[j];
				expectedsMatrix [i][j] = (int) expecteds.get(i)[j];
			}
		}
		
		double expectedResult = Metrics.P_FmacroAvgL(actualsMatrix, expectedsMatrix);
		assertEquals(expectedResult, 1 - actualResult, 0.000001);
	}
}
