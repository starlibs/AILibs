package ai.libs.jaicore.ml.core.timeseries.dataset;

import java.util.Iterator;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.core.timeseries.model.INDArrayTimeseries;

public interface ITimeSeriesInstance extends ILabeledInstance {

	public Iterator<INDArrayTimeseries> iterator();

}
