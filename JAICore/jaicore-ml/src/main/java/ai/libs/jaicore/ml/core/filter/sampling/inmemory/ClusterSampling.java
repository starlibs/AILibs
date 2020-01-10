package ai.libs.jaicore.ml.core.filter.sampling.inmemory;

import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledDataset;
import org.api4.java.ai.ml.core.dataset.supervised.ILabeledInstance;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.ml.core.filter.sampling.SampleElementAddedEvent;

public abstract class ClusterSampling<I extends ILabeledInstance & Clusterable, D extends ILabeledDataset<I>> extends ASamplingAlgorithm<D> {

	protected List<CentroidCluster<I>> clusterResults = null;
	protected int currentCluster = 0;
	protected DistanceMeasure distanceMeassure = new ManhattanDistance();
	protected long seed;

	protected ClusterSampling(final long seed, final D input) {
		super(input);
		this.seed = seed;
	}

	protected ClusterSampling(final long seed, final DistanceMeasure dist, final D input) {
		super(input);
		this.seed = seed;
		this.distanceMeassure = dist;
	}

	public List<CentroidCluster<I>> getClusterResults() {
		return this.clusterResults;
	}

	public void setClusterResults(final List<CentroidCluster<I>> clusterResults) {
		this.clusterResults = clusterResults;
	}

	public void setDistanceMeassure(final DistanceMeasure distanceMeassure) {
		this.distanceMeassure = distanceMeassure;
	}

	public IAlgorithmEvent doAlgorithmStep() throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException {
		if (this.currentCluster < this.clusterResults.size()) {
			CentroidCluster<I> cluster = this.clusterResults.get(this.currentCluster++);
			boolean same = true;
			int n = cluster.getPoints().size();
			for (int i = 1; i < n; i++) {
				if (i % 1000 == 0) {
					this.checkAndConductTermination();
				}
				if (!cluster.getPoints().get(i - 1).getLabel().equals(cluster.getPoints().get(i).getLabel())) {
					same = false;
					break;
				}
			}
			if (same) {
				I near = cluster.getPoints().get(0);
				double dist = Double.MAX_VALUE;
				for (I p : cluster.getPoints()) {
					double newDist = this.distanceMeassure.compute(p.getPoint(), cluster.getCenter().getPoint());
					if (newDist < dist) {
						near = p;
						dist = newDist;
					}
				}
				this.sample.add(near);
			} else {
				// find a solution to not sample all points here
				for (int i = 0; i < cluster.getPoints().size(); i++) {
					this.sample.add(cluster.getPoints().get(i));
				}
			}
			return new SampleElementAddedEvent(this);
		} else {
			return this.terminate();
		}

	}
}
