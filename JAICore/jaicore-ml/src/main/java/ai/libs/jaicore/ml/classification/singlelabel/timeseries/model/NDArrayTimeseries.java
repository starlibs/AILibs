package ai.libs.jaicore.ml.classification.singlelabel.timeseries.model;

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

	@Override
	public int length() {
		return (int) this.getElement().length();
	}

	@Override
	public double[] getPoint() {
		return this.getElement().toDoubleVector();
	}

}
