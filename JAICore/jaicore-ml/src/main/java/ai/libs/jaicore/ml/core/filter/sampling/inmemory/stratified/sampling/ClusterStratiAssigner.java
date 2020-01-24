package ai.libs.jaicore.ml.core.filter.sampling.inmemory.stratified.sampling;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.api4.java.ai.ml.core.dataset.IDataset;
import org.api4.java.ai.ml.core.dataset.IInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ClusterStratiAssigner implements IStratiAssigner {

	private static final Logger LOG = LoggerFactory.getLogger(ClusterStratiAssigner.class);

	private IDataset<?> dataset;

	protected int randomSeed;
	protected DistanceMeasure distanceMeasure;
	private List<CentroidCluster<Clusterable>> clusters;

	public void setDataset(final IDataset<?> dataset) {
		Objects.requireNonNull(dataset);
		if (dataset.isEmpty()) {
			throw new IllegalArgumentException("Cannot compute strati for empty dataset.");
		}
		if (!Clusterable.class.isAssignableFrom(dataset.getClassOfInstances())) {
			boolean allElementsClusterable = dataset.stream().allMatch(Clusterable.class::isInstance);
			if (!allElementsClusterable) {
				throw new IllegalArgumentException("Dataset does contain elements that are not clusterable elements, but only elements of class " + dataset.getClassOfInstances() + ".");
			}
		}
		this.dataset = dataset;
	}

	@Override
	public int assignToStrati(final IInstance datapoint) {
		if (this.dataset == null) {
			throw new IllegalStateException("ClusterStratiAssigner has not been initialized!");
		}
		if (!this.dataset.contains(datapoint)) {
			throw new IllegalArgumentException("Given datapoint " + datapoint + " is not in the original dataset with " + this.dataset.size() + " entries.");
		}

		for (int i = 0; i < this.clusters.size(); i++) {
			List<Clusterable> clusterPoints = this.clusters.get(i).getPoints();
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

	public List<CentroidCluster<Clusterable>> getClusters() {
		return this.clusters;
	}

	protected void setClusters(final List<CentroidCluster<Clusterable>> clusters) {
		this.clusters = clusters;
	}
}
