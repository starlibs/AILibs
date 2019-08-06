package ai.libs.jaicore.ml.core.dataset.attribute.timeseries;

import ai.libs.jaicore.ml.core.dataset.attribute.AAttributeValue;

/**
 * Represents a time series attribute value, as it can be part of a
 * {@link jaicore.ml.core.dataset.IInstance}
 */
public class TimeSeriesAttributeValue extends AAttributeValue<INDArrayTimeseries> implements INDArrayTimeseriesAttributeValue {

	public TimeSeriesAttributeValue(final INDArrayTimeseries value) {
		super(value);
	}

}