package ai.libs.jaicore.ml.core.dataset.attribute.timeseries;

import org.nd4j.linalg.api.ndarray.INDArray;

import ai.libs.jaicore.basic.sets.ElementDecorator;

public class NDArrayTimeseries extends ElementDecorator<INDArray> implements INDArrayTimeseries {

	public NDArrayTimeseries(final INDArray element) {
		super(element);
	}

	@Override
	public INDArray getValue() {
		return this.getElement();
	}

}
