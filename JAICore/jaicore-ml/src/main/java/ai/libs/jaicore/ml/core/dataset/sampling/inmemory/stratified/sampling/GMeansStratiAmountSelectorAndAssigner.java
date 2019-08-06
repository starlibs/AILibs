package ai.libs.jaicore.ml.core.dataset.sampling.inmemory.stratified.sampling;

import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.api4.java.ai.ml.dataset.INumericFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ILabeledInstance;
import org.api4.java.ai.ml.dataset.supervised.INumericFeatureSupervisedDataset;

import ai.libs.jaicore.ml.clustering.GMeans;

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
public class GMeansStratiAmountSelectorAndAssigner<Y, I extends INumericFeatureInstance & ILabeledInstance<Y> & Clusterable, D extends INumericFeatureSupervisedDataset<Y, I>> extends ClusterStratiAssigner<Y, I, D>
		implements IStratiAmountSelector<Double, Y, I, D> {

	private GMeans<I> clusterer;

	/**
	 * Constructor for GMeansStratiAmountSelectorAndAssigner with Manhattan
	 * distanceMeasure as a default.
	 *
	 * @param randomSeed
	 *            Seed for random numbers.
	 */
	public GMeansStratiAmountSelectorAndAssigner(final int randomSeed) {
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
	public GMeansStratiAmountSelectorAndAssigner(final DistanceMeasure distanceMeasure, final int randomSeed) {
		this.randomSeed = randomSeed;
		this.distanceMeasure = distanceMeasure;
	}

	@Override
	public int selectStratiAmount(final D dataset) {
		// Perform g-means to get a fitting k and the corresponding clusters.
		this.clusterer = new GMeans<>(dataset, this.distanceMeasure, this.randomSeed);
		this.clusters = this.clusterer.cluster();
		return this.clusters.size();
	}

	@Override
	public void init(final D dataset, final int stratiAmount) {
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
