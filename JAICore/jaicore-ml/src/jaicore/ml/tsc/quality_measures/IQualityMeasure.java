package jaicore.ml.tsc.quality_measures;

import java.io.Serializable;
import java.util.List;

public interface IQualityMeasure extends Serializable {
	public double assessQuality(final List<Double> distances, final int[] classValues);
}
