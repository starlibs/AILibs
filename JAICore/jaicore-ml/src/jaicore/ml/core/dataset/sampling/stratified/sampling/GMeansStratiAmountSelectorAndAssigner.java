package jaicore.ml.core.dataset.sampling.stratified.sampling;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.ml.clustering.GMeans;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Combined strati amount selector and strati assigner via g-means. IT can be
 * used in 3 combinations:
 * 
 * 1) Amount Selector and Strati Assigner: A g-means
 * clustering is initially performed to select a strati amount via the amount of
 * found clusters and to assign datapoints with this clusters afterwards.
 * 
 * 2) Amount Selector: A g-means clustering is initially performed to select a
 * suitable strati amount with the amount of found clusters.
 * 3) Strati Assigner: Since the amount of strati is selected with another
 * component g-means cannot be used and k-means will be used to be conform with
 * the given strati amount.
 * 
 * It is recommended to use combination 1, because only using this component for
 * only one of the two tasks could yield in inconsistent results.
 * 
 * @author Lukas Brandt
 */
public class GMeansStratiAmountSelectorAndAssigner<I extends IInstance> implements IStratiAssigner<I>, IStratiAmountSelector<I> {

	private static Logger LOG = LoggerFactory.getLogger(GMeansStratiAmountSelectorAndAssigner.class);

	private GMeans<I> clusterer;
	private List<CentroidCluster<I>> clusters;
	private int k;

	private int randomSeed;
	private DistanceMeasure distanceMeasure;

	/**
	 * Constructor for GMeansStratiAmountSelectorAndAssigner with Manhattan
	 * distanceMeasure as a default.
	 * 
	 * @param randomSeed Seed for random numbers.
	 */
	public GMeansStratiAmountSelectorAndAssigner(int randomSeed) {
		this.randomSeed = randomSeed;
		this.distanceMeasure = new ManhattanDistance();
	}

	/**
	 * Constructor for GMeansStratiAmountSelectorAndAssigner with custom
	 * distanceMeasure.
	 * 
	 * @param distanceMeasure Distance measure for datapoints, for example Manhattan
	 *                        or Euclidian.
	 * @param randomSeed      Seed for random numbers.
	 */
	public GMeansStratiAmountSelectorAndAssigner(DistanceMeasure distanceMeasure, int randomSeed) {
		this.randomSeed = randomSeed;
		this.distanceMeasure = distanceMeasure;
	}

	@Override
	public int selectStratiAmount(IDataset<I> dataset) {
		// Perform g-means to get a fitting k and the corresponding clusters.
		this.clusterer = new GMeans<I>(dataset, this.distanceMeasure, randomSeed);
		this.clusters = this.clusterer.cluster();
		this.k = this.clusters.size();
		return this.k;
	}

	@Override
	public void init(IDataset<I> dataset, int stratiAmount) {
		if (this.clusterer == null || this.clusters == null) {
			// This object was not used for strati amount selection.
			// Perform k-means clustering to get the correct strati amounts.
			KMeansPlusPlusClusterer<I> kmeans = new KMeansPlusPlusClusterer<>(stratiAmount, -1,
					this.distanceMeasure, new JDKRandomGenerator(this.randomSeed));
			this.clusters = kmeans.cluster(dataset);
		}
	}

	@Override
	public int assignToStrati(IInstance datapoint) {
		// Search for the cluster that contains the datapoint.
		for (int i = 0; i < this.clusters.size(); i++) {
			List<I> clusterPoints = this.clusters.get(i).getPoints();
			for (int n = 0; n < clusterPoints.size(); n++) {
				if (Arrays.equals(datapoint.getPoint(), clusterPoints.get(n).getPoint())) {
					return i;
				}
			}
		}
		throw new Error("Datapoint was not found in any cluster. This should not happen.");
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
