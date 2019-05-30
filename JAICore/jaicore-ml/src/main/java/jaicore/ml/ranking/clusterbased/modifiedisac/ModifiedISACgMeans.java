package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import jaicore.ml.clustering.GMeans;
import jaicore.ml.ranking.clusterbased.customdatatypes.GroupIdentifier;
import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import weka.core.Instance;

public class ModifiedISACgMeans extends GMeans<DoublePoint> {
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
		super(toClusterPoints.stream().map(DoublePoint::new).collect(Collectors.toList()));
		this.pointToInstance = new HashMap<>();
		for (int i = 0; i < instances.size(); i++) {
			this.pointToInstance.put(toClusterPoints.get(i), instances.get(i));
		}
		this.gmeansCluster = new ArrayList<>();

	}

	public List<Cluster> clusterDeprecated() {
		HashMap<Integer, double[]> positionOfCenter = new HashMap<>();
		int tmp = 1;

		int k = 1;
		int i = 1;
		// creates a k means clustering instance with all points and an L1 distance
		// metric as metric
		ModifiedISACkMeans test = new ModifiedISACkMeans(this.getPoints().stream().map(DoublePoint::getPoint).collect(Collectors.toList()), this.dist);
		// clusters all points with k = 1
		this.currentPoints = test.kmeanscluster(k);
		// puts the first center into the list of center
		for (double[] d : this.currentPoints.keySet()) {
			this.getCentersModifiable().add(d);
		}
		// saves the position of the center for the excess during the g-means clustering
		// algorithm
		for (double[] c : this.getCentersModifiable()) {
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
						} else {
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

		/* make datapoints for merge */
		Map<double[], List<DoublePoint>> mapOfCurrentPoints = new HashMap<>();
		for (Entry<double[], List<double[]>> currentPointMap : this.currentPoints.entrySet()) {
			mapOfCurrentPoints.put(currentPointMap.getKey(), currentPointMap.getValue().stream().map(DoublePoint::new).collect(Collectors.toList()));
		}
		this.mergeCluster(mapOfCurrentPoints);

		for (Entry<double[], List<double[]>> d : this.currentPoints.entrySet()) {
			List<double[]> pointsInCluster = d.getValue();
			List<ProblemInstance<Instance>> instancesInCluster = new ArrayList<>();
			for (double[] point : pointsInCluster) {
				instancesInCluster.add(this.pointToInstance.get(point));
			}
			this.gmeansCluster.add(new Cluster(instancesInCluster, new GroupIdentifier<>(d.getKey())));
		}
		return this.gmeansCluster;
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
