package ai.libs.jaicore.ml.core.timeseries.model;

import org.api4.java.common.timeseries.ITimeseries;
import org.nd4j.linalg.api.ndarray.INDArray;

public interface INDArrayTimeseries extends ITimeseries<INDArray> {

	public int length();

	public double[] getPoint();

}
