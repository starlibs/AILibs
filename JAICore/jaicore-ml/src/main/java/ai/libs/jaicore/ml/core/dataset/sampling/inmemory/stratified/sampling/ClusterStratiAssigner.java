package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.ISupervisedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClusterStratiAssigner<Y, I extends INumericFeatureInstance & ILabeledInstance<Y> & Clusterable, D extends ISupervisedDataset<Double, Y, I>> implements IStratiAssigner<Double, Y, I, D> {

	private static final Logger LOG = LoggerFactory.getLogger(ClusterStratiAssigner.class);

	protected int randomSeed;
	protected DistanceMeasure distanceMeasure;
	protected List<CentroidCluster<I>> clusters;

	@Override
	public int assignToStrati(final I datapoint) {
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
	public void setNumCPUs(final int numberOfCPUs) {
		LOG.warn("setNumCPUs() is not supported for this class");
	}

	@Override
	public int getNumCPUs() {
		return 1;
	}

}
