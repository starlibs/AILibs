package jaicore.ml.evaluation.measures.multiclass;

import java.io.Serializable;

import jaicore.ml.evaluation.measures.ADecomposableDoubleMeasure;

@SuppressWarnings("serial")
public class ZeroOneLoss extends ADecomposableDoubleMeasure<Double> implements Serializable {

	@Override
	public Double calculateMeasure(Double actual, Double expected) {
		return actual == expected ? 0.0 : 1.0;
	}
}
