package ai.libs.jaicore.ml.classification.singlelabel.timeseries.dataset;

import java.util.Iterator;

public interface ITimeSeriesDataset {

	public Iterator<ITimeSeriesInstance> iterator();

}
