package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.ArrayList;
import java.util.List;

/**
 * Compute the inverted F1 measure macro averaged by label. 
 * 
 * @author Helena Graf
 *
 */
public class InverseF1MacroAverageLMultilabelMeasure extends InverseF1MacroAverageDMultilabelMeasure {

	@Override
	public List<Double> calculateMeasure(List<double[]> actual, List<double[]> expected) {
		List<Double> results = new ArrayList<>();
				
		for (int i = 0; i < actual.get(0).length; i++) {
			double[] actualColumn = getIthColumn(actual, i);
			double[] expectedColumn = getIthColumn(expected, i);
			results.add(calculateMeasure(actualColumn, expectedColumn));
		}
		
		return results;		
	}
	
	private double[] getIthColumn(List<double[]> matrix, int i) {
		double[] column = new double[matrix.size()];
		
		for(int j = 0; j < column.length; j++) {
			column[j]=matrix.get(j)[i];
		}
		
		return column;
	}
}
