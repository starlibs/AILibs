package ai.libs.jaicore.ml.core.timeseries.dataset;

import java.util.Iterator;

public interface ITimeSeriesDataset {

	public Iterator<ITimeSeriesInstance> iterator();

}
