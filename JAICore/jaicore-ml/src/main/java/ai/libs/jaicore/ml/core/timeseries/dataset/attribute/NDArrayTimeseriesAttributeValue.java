package ai.libs.jaicore.ml.core.timeseries.dataset.attribute;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ITimeseriesAttributeValue;
import org.api4.java.common.timeseries.ITimeseries;
import org.nd4j.linalg.api.ndarray.INDArray;

public class NDArrayTimeseriesAttributeValue implements ITimeseriesAttributeValue<INDArray> {

	private ITimeseries<INDArray> value;

	public NDArrayTimeseriesAttributeValue(final ITimeseries<INDArray> value) {
		this.value = value;
	}

	@Override
	public ITimeseries<INDArray> getValue() {
		return this.value;
	}

}
