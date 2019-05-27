package jaicore.ml.clustering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

/**
 * Implementation of Gmeans based on Helen Beierlings implementation of
 * GMeans(https://github.com/helebeen/AILibs/blob/master/JAICore/jaicore-modifiedISAC/src/main/java/jaicore/modifiedISAC/ModifiedISACgMeans.java).<br>
 * For more Information see: "Hamerly, G., and Elkan, C. 2003. Learning the k in
 * kmeans. in proceedings of the seventeenth annual conference on neural
 * information processing systems (nips)". <br>
 * <br>
 * This implementation uses {@link KMeansPlusPlusClusterer} as the k-means
 * cluster algorithm.
 *
 * @author Helen Beierling
 * @author jnowack
 *
 * @param <C>
 *            Points to cluster.
 */
public class GMeans<C extends Clusterable> {

	private List<double[]> center = new ArrayList<>();

	private List<CentroidCluster<C>> gmeansCluster = new ArrayList<>();

	private Map<double[], List<C>> currentPoints = new HashMap<>();

	private Map<double[], List<C>> intermediatePoints = new HashMap<>();

	private List<C> points = new ArrayList<>();

	private DistanceMeasure distanceMeasure = new ManhattanDistance();

	private RandomGenerator randomGenerator;

	/**
	 * Initializes a basic cluster for the given Point using Mannhatten distance and
	 * seed=1
	 *
	 * @param toClusterPoints
	 *            Points which should be clustered
	 */
	public GMeans(final Collection<C> toClusterPoints) {
		this(toClusterPoints, new ManhattanDistance(), 1);
	}

	/**
	 * Initializes a cluster for the given Point using a given distance meassure and
	 * a seed.
	 *
	 * @param toClusterPoints
	 *            P
	 * @param distanceMeasure
	 * @param seed
	 */
	public GMeans(final Collection<C> toClusterPoints, final DistanceMeasure distanceMeasure, final long seed) {
		this.points = new ArrayList<>(toClusterPoints);
		this.distanceMeasure = distanceMeasure;
		this.gmeansCluster = new ArrayList<>();
		this.randomGenerator = new JDKRandomGenerator();
		this.randomGenerator.setSeed(seed);
	}

	public List<CentroidCluster<C>> cluster() {
		HashMap<Integer, double[]> positionOfCenter = new HashMap<>();
		int tmp = 1;

		int k = 1;
		int i = 1;
		// creates a k means clustering instance with all points and an L1 distance
		// metric as metric
		KMeansPlusPlusClusterer<C> test = new KMeansPlusPlusClusterer<>(k, -1, this.distanceMeasure, this.randomGenerator);

		// clusters all points with k = 1
		List<CentroidCluster<C>> currentPointsTemp = test.cluster(this.points);
		for (CentroidCluster<C> centroidCluster : currentPointsTemp) {
			this.currentPoints.put(centroidCluster.getCenter().getPoint(), centroidCluster.getPoints());
		}

		// puts the first center into the list of center
		for (double[] d : this.currentPoints.keySet()) {
			this.center.add(d);
		}

		// saves the position of the center for the excess during the g-means clustering
		// algorithm
		for (double[] c : this.center) {
			positionOfCenter.put(tmp, c);
			tmp++;
		}

		while (i <= k) {
			// looppoints are S_i the points are the points of the considered center C_i
			List<C> loopPoints = this.currentPoints.get(positionOfCenter.get(i));

			// makes a new instance with of kmeans with S_i as base
			KMeansPlusPlusClusterer<C> loopCluster = new KMeansPlusPlusClusterer<>(2, -1, this.distanceMeasure, this.randomGenerator);

			// clusters S_I into to cluster intermediate points is a HashMap of center with
			// an ArrayList of thier
			// corresponding points
			List<double[]> intermediateCenter = new ArrayList<>(2);
			if (loopPoints.size() < 2) {
				break;
			}
			List<CentroidCluster<C>> intermediatePointsTemp = loopCluster.cluster(loopPoints);
			for (CentroidCluster<C> centroidCluster : intermediatePointsTemp) {
				this.intermediatePoints.put(centroidCluster.getCenter().getPoint(), centroidCluster.getPoints());
				// intermediate Center saves the found two Center C`_1 und C`_2
				intermediateCenter.add(centroidCluster.getCenter().getPoint());
			}

			// the difference between the two new Center
			double[] v = this.difference(intermediateCenter.get(0), intermediateCenter.get(1));

			double w = 0;
			// w is calculated as the summed squares of the entries of the difference
			// between the center
			// if the entry is NaN it is ignored in the sum
			for (int l = 0; l < v.length; l++) {
				if (!Double.isNaN(v[l])) {
					w += Math.pow(v[l], 2);
				}
			}

			if (w == 0) {
				throw new IllegalStateException("All entries in v are NaN, cannot compute w!");
			}

			double[] y = new double[loopPoints.size()];
			// All points are projected onto a points by multiplying every entry of point
			// with the corresponding
			// entry of v and divide by the w.
			// For every point the all entrys modified that way are than summed.
			// if the entry of v is Nan or the entry of the point the entry is ignored
			for (int r = 0; r < loopPoints.size(); r++) {
				for (int p = 0; p < loopPoints.get(r).getPoint().length; p++) {
					if (!Double.isNaN(loopPoints.get(r).getPoint()[p]) && !Double.isNaN(v[p])) {
						y[r] += (v[p] * loopPoints.get(r).getPoint()[p]) / w;
						// TODO soll ich wenn v an der stelle NaN ist einfach so tuen als w�re es
						// 1 oder nichts machen
					}
				}
			}
			// if the Anderson Darling test is failed the the center C_i is replaced by C`_1
			// and S_i is replaced by the
			// points of C`_1.
			// k is raised by 1.
			// C_k is replaced by C`_2 and the points of C_k S_k are replaced by the one
			// from C`_2 S`_2
			// if the test is passed i is raised.

			if (!this.andersonDarlingTest(y)) {
				this.currentPoints.remove(positionOfCenter.get(i));
				this.currentPoints.put(intermediateCenter.get(0), this.intermediatePoints.get(intermediateCenter.get(0)));
				positionOfCenter.replace(i, intermediateCenter.get(0));
				k++;
				this.currentPoints.put(intermediateCenter.get(1), this.intermediatePoints.get(intermediateCenter.get(1)));
				positionOfCenter.put(k, intermediateCenter.get(1));
			} else {
				i++;
			}
		}
		this.mergeCluster(this.currentPoints);
		for (Entry<double[], List<C>> entry : this.currentPoints.entrySet()) {
			List<C> pointsInCluster = this.currentPoints.get(entry.getKey());
			CentroidCluster<C> c = new CentroidCluster<>(entry::getKey);
			for (C point : pointsInCluster) {
				c.addPoint(point);
			}
			this.gmeansCluster.add(c);
		}
		return this.gmeansCluster;
	}

	protected void mergeCluster(final Map<double[], List<C>> currentPoints) {
		ArrayList<double[]> toMergeCenter = new ArrayList<>();
		for (Entry<double[], List<C>> entry : currentPoints.entrySet()) {
			if (currentPoints.get(entry.getKey()).size() <= 2) {
				toMergeCenter.add(entry.getKey());
			}
		}

		for (double[] d : toMergeCenter) {
			List<C> tmp = currentPoints.remove(d);
			for (C tmpPoints : tmp) {
				double minDist = Double.MAX_VALUE;
				double[] myCenter = null;
				for (double[] c : currentPoints.keySet()) {
					double tmpDist = this.distanceMeasure.compute(tmpPoints.getPoint(), c);
					if (tmpDist <= minDist) {
						myCenter = c;
						minDist = tmpDist;
					}
				}
				currentPoints.get(myCenter).add(tmpPoints);
			}
		}
	}

	protected boolean andersonDarlingTest(final double[] d) {
		// sorts the Array so that the smallest entrys are the first. Entrys are
		// negative too !!
		Arrays.sort(d);

		double mean = 0;
		double variance = 0;

		int totalvalue = 0;
		// mean of the sample is estimated by summing all entries and divide by the
		// total number.
		// Nans are ignored
		for (double i : d) {
			if (!Double.isNaN(i)) {
				totalvalue++;
				mean += i;
			}
		}
		mean = mean / totalvalue;

		totalvalue = 0;
		// variance sigma^2 is estimated by the sum of the squered difference to the
		// mean dvided by sample size -1
		for (double i : d) {
			if (!Double.isNaN(i)) {
				variance += Math.pow((i - mean), 2);
				totalvalue++;
			}
		}
		variance = variance / (totalvalue - 1);
		// the standardization is made by the entries of d subtracted by the mean and
		// divided by the standard deviation
		// if the value of d is NaN the entry in the standardization is also NaN
		double[] y = this.standraizeRandomVariable(d, mean, variance);
		// Are also negative!!
		// total value is equivalent to y.length
		// first part of A^2 is -n overall A^2 = -n-second Part.
		double aSquare1 = (-1.0) * y.length;

		double aSquare2 = 0;
		// creates a normal distribution with mean 0 and standard deviation 1
		NormalDistribution normal = new NormalDistribution(null, 0, 1);
		// if y is not Nan than the second part of A^2 is calculated fist the sum.
		// There are two possible ways to do it but both do not work.

		for (int i = 1; i < y.length; i++) {
			if (!Double.isNaN(y[i])) {
				aSquare2 += ((2 * i) - 1) *

						((Math.log(normal.cumulativeProbability(y[i - 1]))) + Math.log(1 - (normal.cumulativeProbability(y[((y.length) - i)]))));
			}
		}
		// A^2 is divided by the the sample size to complete the second part of A^2^*.
		aSquare2 = aSquare2 / y.length;
		// By substracting part 2 from part 1 A^2^* is completed
		double aSqurestar = aSquare1 - aSquare2;
		// for different sample sizes the threshold weather the distribution is normal
		// or not varies a little.
		// Overall if A^2^* is greater than the threshold than the test fails
		if (y.length <= 10) {
			return aSqurestar <= 0.683;
		} else {
			if (y.length <= 20) {
				return aSqurestar <= 0.704;
			} else {
				if (y.length <= 50) {
					return aSqurestar <= 0.735;
				} else {
					if (y.length <= 100) {
						return aSqurestar <= 0.754;
					} else {
						return aSqurestar <= 0.787;
					}
				}
			}
		}
	}

	private double[] standraizeRandomVariable(final double[] d, final double mean, final double variance) {
		double[] tmp = new double[d.length];
		for (int i = 0; i < tmp.length; i++) {
			if (!Double.isNaN(d[i])) {
				tmp[i] = (d[i] - mean) / (Math.sqrt(variance));
			} else {
				tmp[i] = Double.NaN;
			}
		}
		return tmp;
	}

	protected double[] difference(final double[] a, final double[] b) {
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			// TODO Muss das auch normaliziert werden
			if (!(Double.isNaN(a[i]) || Double.isNaN(b[i]))) {
				c[i] = a[i] - b[i];
			} else {
				c[i] = Double.NaN;
			}
		}
		return c;
	}

	protected List<double[]> getCentersModifiable() {
		return this.center;
	}

	public List<C> getPoints() {
		return this.points;
	}
}
