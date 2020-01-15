package ai.libs.jaicore.ml.classification.singlelabel.timeseries.filter;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset.TimeSeriesDataset2;

public abstract class AFilter implements IFilter {

	@Override
	public TimeSeriesDataset2 fitTransform(final TimeSeriesDataset2 input) {
		this.fit(input);
		return this.transform(input);
	}
}
