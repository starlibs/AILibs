package jaicore.ml.core.evaluation.measure.singlelabel;

import java.io.Serializable;

import com.google.common.math.DoubleMath;

import jaicore.ml.core.evaluation.measure.ADecomposableDoubleMeasure;

public class ZeroOneLoss extends ADecomposableDoubleMeasure<Double> implements Serializable {

	/**
	 * Automatically generated serial version UID for serialization of objects.
	 */
	private static final long serialVersionUID = -1220905293576980035L;

	@Override
	public Double calculateMeasure(final Double actual, final Double expected) {
		return DoubleMath.fuzzyEquals(actual, expected, 1e-6) ? 0.0 : 1.0;
	}
}
