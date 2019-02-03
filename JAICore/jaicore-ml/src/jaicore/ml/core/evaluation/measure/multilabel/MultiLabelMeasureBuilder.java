package jaicore.ml.core.evaluation.measure.multilabel;

import java.util.Arrays;

public class MultiLabelMeasureBuilder {
	private MultiLabelMeasureBuilder() {}
	
	private static double [] convertToDoubleArray(int [] intArray) {
		return Arrays.stream(intArray).mapToDouble(d -> d).toArray();
	}
	
	public static ADecomposableMultilabelMeasure getEvaluator(MultiLabelPerformanceMeasure pm) {
		
		switch (pm) {
		case ZERO_ONE : return new ADecomposableMultilabelMeasure() {
			
			ZeroOneLossMultilabelMeasure base = new ZeroOneLossMultilabelMeasure();
			
			@Override
			public Double calculateMeasure(int[] actual, int[] expected) {
				return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
			}
		};
		case INVERSE_F1_MACRO_AVERAGE_D:
			return new ADecomposableMultilabelMeasure() {
				
				private InverseF1MacroAverageDMultilabelMeasure base = new InverseF1MacroAverageDMultilabelMeasure();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		case INVERSE_F1_MACRO_AVERAGE_L:
			return new ADecomposableMultilabelMeasure() {
				
				private InverseF1MacroAverageLMultilabelMeasure base = new InverseF1MacroAverageLMultilabelMeasure();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		case HAMMING:
			return new ADecomposableMultilabelMeasure() {
				
				private HammingLossMultilabelEvaluator base = new HammingLossMultilabelEvaluator();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		case JACCARD:
			return new ADecomposableMultilabelMeasure() {
				
				private JaccardErrorMultilabelMeasure base = new JaccardErrorMultilabelMeasure();
				
				@Override
				public Double calculateMeasure(int[] actual, int[] expected) {
					return base.calculateMeasure(convertToDoubleArray(actual), convertToDoubleArray(expected));
				}
			};
		case RANK:
			return new ADecomposableMultilabelMeasure() {
				
				private RankLossMultilabelEvaluator base = new RankLossMultilabelEvaluator();
				
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
