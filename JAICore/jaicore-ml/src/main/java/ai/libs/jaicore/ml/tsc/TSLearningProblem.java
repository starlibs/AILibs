package ai.libs.jaicore.ml.tsc;

import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;
import ai.libs.jaicore.ml.tsc.quality_measures.IQualityMeasure;

public class TSLearningProblem {
	private final IQualityMeasure qualityMeasure;
	private final TimeSeriesDataset dataset;

	public TSLearningProblem(final IQualityMeasure qualityMeasure, final TimeSeriesDataset dataset) {
		super();
		this.qualityMeasure = qualityMeasure;
		this.dataset = dataset;
	}

	public IQualityMeasure getQualityMeasure() {
		return this.qualityMeasure;
	}

	public TimeSeriesDataset getDataset() {
		return this.dataset;
	}
}
