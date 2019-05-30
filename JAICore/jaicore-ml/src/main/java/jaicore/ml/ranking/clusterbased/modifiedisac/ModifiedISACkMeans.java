package jaicore.ml.ranking.clusterbased.modifiedisac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifiedISACkMeans extends Kmeans<double[], Double> {
	private Map<double[], Integer> positionOfPointInList = new HashMap<>();

	private Map<double[], List<double[]>> pointsInCenter = new HashMap<>();
	private Map<double[], double[]> centerOfPoint = new HashMap<>();
	private List<double[]> initpoints = new ArrayList<>();

	public ModifiedISACkMeans(final List<double[]> toClusterPoints, final IDistanceMetric<Double, double[], double[]> dist) {
		super(toClusterPoints, dist);
		for (int i = 0; i < toClusterPoints.size(); i++) {
			this.positionOfPointInList.put(toClusterPoints.get(i), i);
		}
		this.initpoints = new ArrayList<>(toClusterPoints);
	}

	@Override
	public Map<double[], List<double[]>> kmeanscluster(final int k) {
		this.k = k;
		this.initializeKMeans();
		boolean test = true;
		while (test) {
			this.relocateCenter();
			test = this.relocatePoints();
		}
		return this.pointsInCenter;
	}

	@Override
	public void initializeKMeans() {
		this.initializeFirstCenter();
		this.initializeFollowingCenter();
		this.locateFirstPoints();
	}

	private void initializeFirstCenter() {
		double[] firstCenter = new double[this.points.get(0).length];
		for (int i = 0; i < this.points.get(0).length; i++) {
			int totalvalue = this.points.size();
			for (double[] d : this.points) {
				if (Double.isNaN(d[i])) {
					totalvalue--;
				} else {
					firstCenter[i] += d[i];
				}
			}
			firstCenter[i] = firstCenter[i] / totalvalue;
		}
		this.center.add(firstCenter);
		this.pointsInCenter.put(firstCenter, new ArrayList<double[]>());
	}

	private void locateFirstPoints() {
		for (double[] point : this.points) {
			int indexOfMyCenter = 0;
			double maxCenterDist = this.metric.computeDistance(point, this.center.get(0));
			for (int i = 1; i < this.center.size(); i++) {
				double tmp = this.metric.computeDistance(point, this.center.get(i));
				if (tmp <= maxCenterDist) {
					indexOfMyCenter = i;
					maxCenterDist = tmp;
				}
			}
			this.centerOfPoint.put(point, this.center.get(indexOfMyCenter));
			this.pointsInCenter.get(this.center.get(indexOfMyCenter)).add(point);
		}
	}

	private boolean relocatePoints() {
		boolean hasSomethingChanged = false;
		for (double[] c : this.center) {
			this.pointsInCenter.get(c).clear();
		}
		for (double[] point : this.points) {
			double minDist = this.metric.computeDistance(point, this.center.get(0));
			int indexOfMyCenter = 0;
			for (int i = 1; i < this.center.size(); i++) {
				double tmp = this.metric.computeDistance(point, this.center.get(i));
				if (tmp < minDist) {
					indexOfMyCenter = i;
					minDist = tmp;
				}
			}
			if (!Arrays.equals(this.centerOfPoint.get(point), this.center.get(indexOfMyCenter))) {
				hasSomethingChanged = true;
				this.centerOfPoint.replace(point, this.center.get(indexOfMyCenter));
			}
			this.pointsInCenter.get(this.center.get(indexOfMyCenter)).add(point);
		}
		return hasSomethingChanged;
	}

	private void relocateCenter() {
		for (int i = 0; i < this.center.size(); i++) {
			int size = this.center.get(i).length;
			double[] sumarray = new double[size];
			double[] totalvalue = new double[size];
			if (!this.pointsInCenter.get(this.center.get(i)).isEmpty()) {
				for (double[] d : this.pointsInCenter.get(this.center.get(i))) {
					for (int j = 0; j < d.length; j++) {
						if (!Double.isNaN(d[j])) {
							sumarray[j] += d[j];
							totalvalue[j]++;
						}
					}
				}

				for (int l = 0; l < sumarray.length; l++) {
					if (Double.isNaN(sumarray[l])) {
						sumarray[l] = 0;
					} else {
						if (!(sumarray[l] == 0 || totalvalue[l] == 0)) {
							sumarray[l] = sumarray[l] / totalvalue[l];
						} else {
							if (totalvalue[l] == 0) {
								sumarray[l] = Double.NaN;
							}
						}

					}
				}
			}
			List<double[]> myPoints = this.pointsInCenter.remove(this.center.get(i));
			this.pointsInCenter.put(sumarray, myPoints);
			this.center.set(i, sumarray);
		}

	}

	private void initializeFollowingCenter() {
		for (int i = 1; i < this.k; i++) {
			double maxsum = 0;
			int indexOfnewCenter = 0;
			int indexofnewCenterinInit = 0;
			for (int j = 0; j < this.initpoints.size(); j++) {
				double tmp = 0;
				for (double[] c : this.center) {
					tmp += this.metric.computeDistance(this.initpoints.get(j), c);
				}
				if (tmp >= maxsum) {
					maxsum = tmp;
					indexOfnewCenter = this.positionOfPointInList.get(this.initpoints.get(j));
					indexofnewCenterinInit = j;
				}
			}
			this.initpoints.remove(indexofnewCenterinInit);
			this.center.add(this.points.get(indexOfnewCenter));

			this.pointsInCenter.put(this.points.get(indexOfnewCenter), new ArrayList<double[]>());
		}
	}

	public List<double[]> getCenter() {
		return this.center;
	}
}
