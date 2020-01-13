package ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset;

import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;

import ai.libs.jaicore.ml.classification.singlelabel.timeseries.model.INDArrayTimeseries;
import ai.libs.jaicore.ml.core.filter.sampling.IClusterableInstance;

public interface ITimeSeriesInstance extends IClusterableInstance, ILabeledInstance, Iterable<INDArrayTimeseries> {

}
