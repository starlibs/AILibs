package jaicore.modifiedISAC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ModifiedISACkMeans extends Kmeans<double[], Double> {
	private HashMap<double[], Integer> PositionOfPointInList = new HashMap<double[], Integer>();

	private HashMap<double[], ArrayList<double[]>> pointsInCenter = new HashMap<double[], ArrayList<double[]>>();
	private HashMap<double[], double[]> CenterOfPoint = new HashMap<double[], double[]>();
	private ArrayList<double[]> initpoints = new ArrayList<double[]>();


	public ModifiedISACkMeans(ArrayList<double[]> toClusterPoints, IDistanceMetric<Double, double[], double[]> dist) {
		super(toClusterPoints, dist);
		for (int i = 0; i < toClusterPoints.size(); i++) {
			PositionOfPointInList.put(toClusterPoints.get(i), i);
		}
		initpoints = (ArrayList<double[]>) toClusterPoints.clone();
	}

	@Override
	public HashMap<double[],ArrayList<double[]>> kmeanscluster(int k) {
		this.k = k;
		initializeKMeans();
		boolean test = true;
		while (test) {
			relocateCenter();
			test = relocatePoints();
		}
		return pointsInCenter;
	}

	@Override
	public void initializeKMeans() {
		initializeFirstCenter();
		initializeFollowingCenter();
		locateFirstPoints();
	}

	private void initializeFirstCenter() {
		double[] firstCenter = new double[points.get(0).length];
		for (int i = 0; i < points.get(0).length; i++) {
			int totalvalue = points.size();
			for (double[] d : points) {
				if (Double.isNaN(d[i])) {
					totalvalue--;
				} else {
					firstCenter[i] += d[i];
				}
			}
			firstCenter[i] = firstCenter[i] / totalvalue;
		}
		center.add(firstCenter);
		pointsInCenter.put(firstCenter, new ArrayList<double[]>());
	}

	private void locateFirstPoints() {
		try {
			for (double[] point : points) {
				int indexOfMyCenter = 0;
				double maxCenterDist = metric.computeDistance(point, center.get(0));
				for (int i = 1; i < center.size(); i++) {
					double tmp = metric.computeDistance(point, center.get(i));
					if (tmp <= maxCenterDist) {
						indexOfMyCenter = i;
						maxCenterDist = tmp;
					}
				}
				CenterOfPoint.put(point, center.get(indexOfMyCenter));
				pointsInCenter.get(center.get(indexOfMyCenter)).add(point);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private boolean relocatePoints() {
		boolean hasSomethingChanged = false;
		for (double[] c : center) {
			pointsInCenter.get(c).clear();
		}
		try {
			for (double[] point : points) {
				double minDist = metric.computeDistance(point, center.get(0));
				int indexOfMyCenter = 0;
				for (int i = 1; i < center.size(); i++) {
					double tmp = metric.computeDistance(point, center.get(i));
					if (tmp < minDist) {
						indexOfMyCenter = i;
						minDist = tmp;
					}
				}
				if (!Arrays.equals(CenterOfPoint.get(point), center.get(indexOfMyCenter))) {
					hasSomethingChanged = true;
					CenterOfPoint.replace(point, center.get(indexOfMyCenter));
				}
				pointsInCenter.get(center.get(indexOfMyCenter)).add(point);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return hasSomethingChanged;
	}

	private void relocateCenter() {
		for (int i = 0; i < center.size(); i++) {
			int size = center.get(i).length;
			double[] sumarray = new double[size];
			double[] totalvalue = new double[size];
			if (!pointsInCenter.get(center.get(i)).isEmpty()) {
				for (double[] d : pointsInCenter.get(center.get(i))) {
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
			ArrayList<double[]> myPoints = pointsInCenter.remove(center.get(i));
			pointsInCenter.put(sumarray, myPoints);
			center.set(i, sumarray);
		}

	}

	private void initializeFollowingCenter() {
		for (int i = 1; i < k; i++) {
			double maxsum = 0;
			int indexOfnewCenter = 0;
			int indexofnewCenterinInit = 0;
			for (int j = 0; j < initpoints.size(); j++) {
				double tmp = 0;
				for (double[] c : center) {
					try {
						tmp += metric.computeDistance(initpoints.get(j), c);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (tmp >= maxsum) {
					maxsum = tmp;
					indexOfnewCenter = PositionOfPointInList.get(initpoints.get(j));
					indexofnewCenterinInit = j;
				}
			}
			initpoints.remove(indexofnewCenterinInit);
			center.add(points.get(indexOfnewCenter));
			
			pointsInCenter.put(points.get(indexOfnewCenter), new ArrayList<double[]>());
		}
	}
	public ArrayList<double[]> getCenter(){
		return center;
	}
}
