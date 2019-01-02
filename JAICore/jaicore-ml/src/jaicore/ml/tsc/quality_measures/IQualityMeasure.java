package jaicore.ml.tsc.quality_measures;

import java.io.Serializable;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;

public interface IQualityMeasure extends Serializable {
	public double assessQuality(final List<Double> distances, final INDArray classValues);
}
