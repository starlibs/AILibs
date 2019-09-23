package ai.libs.jaicore.ml.core.timeseries.util;

import ai.libs.jaicore.ml.core.timeseries.dataset.TimeSeriesDataset2;
import ai.libs.jaicore.ml.core.timeseries.loss.IQualityMeasure;

public class TSLearningProblem {
	private final IQualityMeasure qualityMeasure;
	private final TimeSeriesDataset2 dataset;

	public TSLearningProblem(final IQualityMeasure qualityMeasure, final TimeSeriesDataset2 dataset) {
		super();
		this.qualityMeasure = qualityMeasure;
		this.dataset = dataset;
	}

	public IQualityMeasure getQualityMeasure() {
		return this.qualityMeasure;
	}

	public TimeSeriesDataset2 getDataset() {
		return this.dataset;
	}
}
