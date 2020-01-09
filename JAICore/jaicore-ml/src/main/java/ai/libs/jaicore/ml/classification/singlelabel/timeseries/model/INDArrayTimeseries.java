package ai.libs.jaicore.ml.classification.singlelabel.timeseries.model;

import org.api4.java.ai.ml.core.dataset.schema.attribute.ITimeseries;
import org.nd4j.linalg.api.ndarray.INDArray;

public interface INDArrayTimeseries extends ITimeseries<INDArray> {

	public int length();

	public double[] getPoint();

}
