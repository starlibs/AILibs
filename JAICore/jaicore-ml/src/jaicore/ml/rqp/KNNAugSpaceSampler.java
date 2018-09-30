package jaicore.ml.rqp;

import java.util.ArrayList;
import java.util.Random;

import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.NearestNeighbourSearch;

/**
 * Samples interval-valued data from a dataset of precise points. 
 * First chooses one point uniformly at random and then generates a point in the interval-valued augmented space
 * from it and its (exact) K nearest neighbors according to euclidean distance on the attributes, excluding the class,
 * which is assumed to be the last attribute.
 * @author Michael
 *
 */
public class KNNAugSpaceSampler extends AbstractAugmentedSpaceSampler {
	
	private final NearestNeighbourSearch nearestNeighbour;
	private int k;
	
	/**
	 * @param nearestNeighbour The nearest neighbour search algorithm to use.
	 * @author Michael
	 *
	 */
	public KNNAugSpaceSampler(Instances preciseInsts, Random rng, int k, NearestNeighbourSearch nearestNeighbour) {
		super(preciseInsts, rng);
		this.k = k;
		DistanceFunction dist = new EuclideanDistance(preciseInsts);
		String distOptionColumns = String.format("-R first-%d", preciseInsts.numAttributes() - 1);
		String[] distOptions = {distOptionColumns};
		
		try {
			dist.setOptions(distOptions);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			nearestNeighbour.setDistanceFunction(dist);
			nearestNeighbour.setInstances(preciseInsts);
		} catch (Exception e) {
			e.printStackTrace();
		}
		nearestNeighbour.setMeasurePerformance(false);
		this.nearestNeighbour = nearestNeighbour;
	}

	@Override
	public Instance augSpaceSample() {
		Instances preciseInsts = this.getPreciseInsts();
		int numInsts = preciseInsts.size();
		
		Instance x = preciseInsts.get(this.getRng().nextInt(numInsts));		
		Instances kNNs = null;	
		try {
			kNNs = nearestNeighbour.kNearestNeighbours(x, k);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ArrayList<Instance> sampledPoints = new ArrayList<Instance>();
		sampledPoints.add(x);
		sampledPoints.addAll(kNNs);
		
		return generateAugPoint(sampledPoints);
	}

	/**
	 * @return the k
	 */
	public int getK() {
		return k;
	}

	/**
	 * @param k the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}

}
