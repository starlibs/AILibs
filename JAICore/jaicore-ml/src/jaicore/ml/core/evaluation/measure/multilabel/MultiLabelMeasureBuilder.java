package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;

public class MultiLabelMeasureBuilder {
	private MultiLabelMeasureBuilder() {}
	
	private static double [] convertToDoubleArray(int [] intArray) {
		return Arrays.stream(intArray).mapToDouble(d -> d).toArray();
	}
	
	public static ADecomposableMultilabelMeasure getEvaluator(MultiLabelPerformanceMeasure pm) {
		
		switch (pm) {
		case EXACT_MATCH : return new ADecomposableMultilabelMeasure() {
			
			ExactMatchLoss base = new ExactMatchLoss();
			
			@Override
			public Double calculateMeasure(int[] actual, int[] expected) {
				return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
			}
		};
		case F1_AVERAGE:
			return new ADecomposableMultilabelMeasure() {
				
				private F1AverageMeasure base = new F1AverageMeasure();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		case HAMMING:
			return new ADecomposableMultilabelMeasure() {
				
				private HammingMultilabelEvaluator base = new HammingMultilabelEvaluator();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		case JACCARD:
			return new ADecomposableMultilabelMeasure() {
				
				private JaccardMultilabelEvaluator base = new JaccardMultilabelEvaluator();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		case RANK:
			return new ADecomposableMultilabelMeasure() {
				
				private RankMultilabelEvaluator base = new RankMultilabelEvaluator();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		default:
			throw new IllegalArgumentException("No support for performance measure " + pm);
		}
	}
}
