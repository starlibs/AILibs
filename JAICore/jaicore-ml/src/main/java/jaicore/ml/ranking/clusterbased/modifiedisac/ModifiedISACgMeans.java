package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.NormalDistribution;

import jaicore.ml.ranking.clusterbased.customdatatypes.GroupIdentifier;
import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import weka.core.Instance;

public class ModifiedISACgMeans extends Gmeans<double[]> {
	private List<Cluster> gmeansCluster;
	private List<double[]> intermediateCenter;
	private Map<double[], List<double[]>> currentPoints;
	private Map<double[], List<double[]>> intermediatePoints;
	private List<double[]> loopPoints;
	private Map<double[], ProblemInstance<Instance>> pointToInstance;
	L1DistanceMetric dist = new L1DistanceMetric();

	/**
	 * inilizes toClusterPoints with the points that are to Cluster and are
	 * normalized metafeatures
	 *
	 * @param toClusterPoints
	 * @param instances
	 */
	public ModifiedISACgMeans(final List<double[]> toClusterPoints, final List<ProblemInstance<Instance>> instances) {
		super(toClusterPoints);
		this.pointToInstance = new HashMap<>();
		for (int i = 0; i < instances.size(); i++) {
			this.pointToInstance.put(toClusterPoints.get(i), instances.get(i));
		}
		this.gmeansCluster = new ArrayList<>();

	}

	@Override
	public List<Cluster> gmeanscluster() {
		HashMap<Integer, double[]> positionOfCenter = new HashMap<>();
		int tmp = 1;

		int k = 1;
		int i = 1;
		// creates a k means clustering instance with all points and an L1 distance
		// metric as metric
		ModifiedISACkMeans test = new ModifiedISACkMeans(this.points, this.dist);
		// clusters all points with k = 1
		this.currentPoints = test.kmeanscluster(k);
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
			this.loopPoints = this.currentPoints.get(positionOfCenter.get(i));
			// makes a new instance with of kmeans with S_i as base
			ModifiedISACkMeans loopCluster = new ModifiedISACkMeans(this.loopPoints, this.dist);
			// clusters S_I into to cluster intermediate points is a HashMap of center with an ArrayList of thier corresponding points
			this.intermediatePoints = loopCluster.kmeanscluster(2);
			// intermediate Center saves the found two Center C`_1 und C`_2
			this.intermediateCenter = loopCluster.getCenter();

			// the difference between the two new Center
			double[] v = this.difference(this.intermediateCenter.get(0), this.intermediateCenter.get(1));

			double w = 0;
			// w is calculated as the summed squares of the entries of the difference
			// between the center
			// if the entry is NaN it is ignored in the sum
			for (int l = 0; l < v.length; l++) {
				if (!Double.isNaN(v[l])) {
					w += Math.pow(v[l], 2);
				}
			}

			double[] y = new double[this.loopPoints.size()];
			// All points are projected onto a points by multiplying every entry of point
			// with the corresponding
			// entry of v and divide by the w.
			// For every point the all entrys modified that way are than summed.
			// if the entry of v is Nan or the entry of the point the entry is ignored
			for (int r = 0; r < this.loopPoints.size(); r++) {
				for (int p = 0; p < this.loopPoints.get(r).length; p++) {
					if (!Double.isNaN(this.loopPoints.get(r)[p])) {
						if (!Double.isNaN(v[p]) && w != 0) {
							y[r] += (v[p] * this.loopPoints.get(r)[p]) / w;
						}
						else {
							throw new UnsupportedOperationException("We have not covered this case yet!");
						}
					}
				}
			}
			// if the Anderson Darling test is failed the the center C_i is replaced by C`_1
			// and S_i is replaced by the points of C`_1. k is raised by 1.
			// C_k is replaced by C`_2 and the points of C_k S_k are replaced by the one
			// from C`_2 S`_2 if the test is passed i is raised.
			if (!this.andersonDarlingTest(y)) {
				this.currentPoints.remove(positionOfCenter.get(i));
				this.currentPoints.put(this.intermediateCenter.get(0), this.intermediatePoints.get(this.intermediateCenter.get(0)));
				positionOfCenter.replace(i, this.intermediateCenter.get(0));
				k++;
				this.currentPoints.put(this.intermediateCenter.get(1), this.intermediatePoints.get(this.intermediateCenter.get(1)));
				positionOfCenter.put(k, this.intermediateCenter.get(1));
			} else {
				i++;
			}
		}
		this.mergeCluster(this.currentPoints);
		for (Entry<double[],List<double[]>> d : this.currentPoints.entrySet()) {
			List<double[]> pointsInCluster = d.getValue();
			List<ProblemInstance<Instance>> instancesInCluster = new ArrayList<>();
			for (double[] point : pointsInCluster) {
				instancesInCluster.add(this.pointToInstance.get(point));
			}
			this.gmeansCluster.add(new Cluster(instancesInCluster, new GroupIdentifier<>(d.getKey())));
		}
		return this.gmeansCluster;
	}

	private void mergeCluster(final Map<double[], List<double[]>> currentPoints) {
		List<double[]> toMergeCenter = new ArrayList<>();
		for (Entry<double[], List<double[]>> centerWithItsPoints : currentPoints.entrySet()) {
			if (centerWithItsPoints.getValue().size() <= 2) {
				toMergeCenter.add(centerWithItsPoints.getKey());
			}
		}
		for (double[] d : toMergeCenter) {
			List<double[]> tmp = currentPoints.remove(d);
			for (double[] points : tmp) {
				double minDist = Double.MAX_VALUE;
				double[] myCenter = null;
				for (double[] c : currentPoints.keySet()) {
					double tmpDist = this.dist.computeDistance(points, c);
					if (tmpDist <= minDist) {
						myCenter = c;
						minDist = tmpDist;
					}
				}
				currentPoints.get(myCenter).add(points);
			}
		}

	}

	private boolean andersonDarlingTest(final double[] d) {
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
				aSquare2 += ((2 * i) - 1) * ((Math.log(normal.cumulativeProbability(y[i - 1]))) + Math.log(1 - (normal.cumulativeProbability(y[((y.length) - i)]))));
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

	private double[] difference(final double[] a, final double[] b) {
		double[] c = new double[a.length];
		for (int i = 0; i < a.length; i++) {
			if (!(Double.isNaN(a[i]) || Double.isNaN(b[i]))) {
				c[i] = a[i] - b[i];
			} else {
				c[i] = Double.NaN;
			}
		}
		return c;
	}

	public List<Cluster> getGmeansCluster() {
		return this.gmeansCluster;
	}

	public List<double[]> getIntermediateCenter() {
		return this.intermediateCenter;
	}

	public Map<double[], List<double[]>> getCurrentPoints() {
		return this.currentPoints;
	}

	public Map<double[], List<double[]>> getIntermediatePoints() {
		return this.intermediatePoints;
	}

	public List<double[]> getLoopPoints() {
		return this.loopPoints;
	}

	public Map<double[], ProblemInstance<Instance>> getPointToInstance() {
		return this.pointToInstance;
	}
}
