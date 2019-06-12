package ai.libs.jaicore.ml.tsc.filter;

import ai.libs.jaicore.ml.tsc.dataset.TimeSeriesDataset;

public abstract class AFilter implements IFilter {

	@Override
	public TimeSeriesDataset fitTransform(final TimeSeriesDataset input) {
		this.fit(input);
		return this.transform(input);
	}
}
