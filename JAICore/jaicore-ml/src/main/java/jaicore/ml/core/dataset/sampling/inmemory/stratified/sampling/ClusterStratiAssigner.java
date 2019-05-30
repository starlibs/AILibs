package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.INumericArrayInstance;

public abstract class ClusterStratiAssigner<I extends INumericArrayInstance, D extends IDataset<I>> implements IStratiAssigner<I, D> {

	private static final Logger LOG = LoggerFactory.getLogger(ClusterStratiAssigner.class);

	protected int randomSeed;
	protected DistanceMeasure distanceMeasure;
	protected List<CentroidCluster<I>> clusters;

	@Override
	public int assignToStrati(I datapoint) {
		// Search for the cluster that contains the datapoint.
		for (int i = 0; i < this.clusters.size(); i++) {
			List<I> clusterPoints = this.clusters.get(i).getPoints();
			for (int n = 0; n < clusterPoints.size(); n++) {
				if (Arrays.equals(datapoint.getPoint(), clusterPoints.get(n).getPoint())) {
					return i;
				}
			}
		}
		throw new IllegalStateException("Datapoint was not found in any cluster. This should not happen.");
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		LOG.warn("setNumCPUs() is not supported for this class");
	}

	@Override
	public int getNumCPUs() {
		return 1;
	}

}
