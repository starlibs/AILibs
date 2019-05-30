package jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;

import jaicore.ml.clustering.GMeans;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.INumericArrayInstance;

/**
 * Combined strati amount selector and strati assigner via g-means. IT can be
 * used in 3 combinations:
 * 
 * 1) Amount Selector and Strati Assigner: A g-means clustering is initially
 * performed to select a strati amount via the amount of found clusters and to
 * assign datapoints with this clusters afterwards.
 * 
 * 2) Amount Selector: A g-means clustering is initially performed to select a
 * suitable strati amount with the amount of found clusters. 3) Strati Assigner:
 * Since the amount of strati is selected with another component g-means cannot
 * be used and k-means will be used to be conform with the given strati amount.
 * 
 * It is recommended to use combination 1, because only using this component for
 * only one of the two tasks could yield in inconsistent results.
 * 
 * @author Lukas Brandt
 */
public class GMeansStratiAmountSelectorAndAssigner<I extends INumericArrayInstance, D extends IDataset<I>> extends ClusterStratiAssigner<I, D> implements IStratiAmountSelector<D> {

	private GMeans<I> clusterer;

	/**
	 * Constructor for GMeansStratiAmountSelectorAndAssigner with Manhattan
	 * distanceMeasure as a default.
	 * 
	 * @param randomSeed
	 *            Seed for random numbers.
	 */
	public GMeansStratiAmountSelectorAndAssigner(int randomSeed) {
		this.randomSeed = randomSeed;
		this.distanceMeasure = new ManhattanDistance();
	}

	/**
	 * Constructor for GMeansStratiAmountSelectorAndAssigner with custom
	 * distanceMeasure.
	 * 
	 * @param distanceMeasure
	 *            Distance measure for datapoints, for example Manhattan or
	 *            Euclidian.
	 * @param randomSeed
	 *            Seed for random numbers.
	 */
	public GMeansStratiAmountSelectorAndAssigner(DistanceMeasure distanceMeasure, int randomSeed) {
		this.randomSeed = randomSeed;
		this.distanceMeasure = distanceMeasure;
	}

	@Override
	public int selectStratiAmount(D dataset) {
		// Perform g-means to get a fitting k and the corresponding clusters.
		this.clusterer = new GMeans<>(dataset, this.distanceMeasure, randomSeed);
		this.clusters = this.clusterer.cluster();
		return this.clusters.size();
	}

	@Override
	public void init(D dataset, int stratiAmount) {
		if (this.clusterer == null || this.clusters == null) {
			// This object was not used for strati amount selection.
			// Perform k-means clustering to get the correct strati amounts.
			JDKRandomGenerator rand = new JDKRandomGenerator();
			rand.setSeed(this.randomSeed);
			KMeansPlusPlusClusterer<I> kmeans = new KMeansPlusPlusClusterer<>(stratiAmount, -1, this.distanceMeasure, rand);
			this.clusters = kmeans.cluster(dataset);
		}
	}

}
