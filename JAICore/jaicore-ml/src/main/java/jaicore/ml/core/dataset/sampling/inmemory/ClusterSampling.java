package jaicore.ml.core.dataset.sampling.inmemory;

import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.INumericLabeledAttributeArrayInstance;
import jaicore.ml.core.dataset.sampling.SampleElementAddedEvent;

public abstract class ClusterSampling<I extends INumericLabeledAttributeArrayInstance<? extends Number>, D extends IDataset<I>> extends ASamplingAlgorithm<D> {

	protected List<CentroidCluster<I>> clusterResults = null;
	protected int currentCluster = 0;
	protected DistanceMeasure distanceMeassure = new ManhattanDistance();
	protected long seed;

	protected ClusterSampling(long seed, D input) {
		super(input);
		this.seed = seed;
	}

	protected ClusterSampling(long seed, DistanceMeasure dist, D input) {
		super(input);
		this.seed = seed;
		this.distanceMeassure = dist;
	}

	public List<CentroidCluster<I>> getClusterResults() {
		return clusterResults;
	}

	public void setClusterResults(List<CentroidCluster<I>> clusterResults) {
		this.clusterResults = clusterResults;
	}

	public void setDistanceMeassure(DistanceMeasure distanceMeassure) {
		this.distanceMeassure = distanceMeassure;
	}

	public AlgorithmEvent doAlgorithmStep() {
		if (currentCluster < clusterResults.size()) {
			CentroidCluster<I> cluster = clusterResults.get(currentCluster++);
			boolean same = true;
			for (int i = 1; i < cluster.getPoints().size(); i++) {
				if (!cluster.getPoints().get(i - 1).getTargetValue().equals(cluster.getPoints().get(i).getTargetValue())) {
					same = false;
					break;
				}
			}
			if (same) {
				I near = cluster.getPoints().get(0);
				double dist = Double.MAX_VALUE;
				for (I p : cluster.getPoints()) {
					double newDist = distanceMeassure.compute(p.getPoint(), cluster.getCenter().getPoint());
					if (newDist < dist) {
						near = p;
						dist = newDist;
					}
				}
				sample.add(near);
			} else {
				// find a solution to not sample all points here
				for (int i = 0; i < cluster.getPoints().size(); i++) {
					sample.add(cluster.getPoints().get(i));
				}
			}
			return new SampleElementAddedEvent(getId());
		} else {
			return this.terminate();
		}

	}
}
