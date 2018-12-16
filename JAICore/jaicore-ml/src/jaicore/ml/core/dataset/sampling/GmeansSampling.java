package jaicore.ml.core.dataset.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.ManhattanDistance;
import org.apache.commons.math3.random.JDKRandomGenerator;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.ml.clustering.GMeans;
import jaicore.ml.core.SimpleInstanceImpl;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;
import jaicore.ml.core.dataset.InstanceSchema;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeType;
import jaicore.ml.core.dataset.attribute.primitive.NumericAttributeValue;
import jaicore.ml.core.dataset.standard.SimpleDataset;
import jaicore.ml.core.dataset.standard.SimpleInstance;

/**
 * Implementation of a sampling method using gmeans-clustering. Does only work
 * with {@link SimpleInstance}s as data. We need a way to create empty datsets
 * and an Instance from a double vector to have this working for all
 * {@link IInstance}s.
 * 
 * @author jnowack
 *
 */
public class GmeansSampling extends ASamplingAlgorithm {

	private GMeans<IInstance> gMeansCluster;
	private List<CentroidCluster<IInstance>> clusterResults;

	private long seed;
	
	/**
	 * Implementation of a sampling method using gmeans-clustering.
	 */
	public GmeansSampling(long seed) {
		this.seed = seed;
	}

	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			// Initialize variables
			this.sample = this.createEmptyDatasetFromInputSchema();

			// create cluster
			gMeansCluster = new GMeans<IInstance>(getInput(), new ManhattanDistance(), seed);
			clusterResults = gMeansCluster.cluster();

			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();
		case active:
			for (CentroidCluster<IInstance> cluster : clusterResults) {
				boolean same = true;
				for (int i = 1; i < cluster.getPoints().size(); i++) {
					if (!cluster.getPoints().get(i - 1).getTargetValue(Double.class)
							.equals(cluster.getPoints().get(i).getTargetValue(Double.class))) {
						same = false;
						break;
					}
				}
				if (same) {
					// if all points are the same only add the center 
					sample.add(createSimpleInstanceFromDoubleVector(cluster.getCenter().getPoint(), (NumericAttributeValue) cluster.getPoints().get(0).getTargetValue(Double.class)));
					
				} else {
					for (int i = 0; i < cluster.getPoints().size(); i++) {
						sample.add(cluster.getPoints().get(i));
					}
				}
			}
			this.setState(AlgorithmState.inactive);
			return new AlgorithmFinishedEvent();
		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new Exception("Expected sample size was not reached before termination");
			} else {
				return new AlgorithmFinishedEvent();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
	}
	
	
	
	private SimpleInstance createSimpleInstanceFromDoubleVector(double[] input, NumericAttributeValue target) {
		int i = 0;
		ArrayList<IAttributeValue<?>> values = new ArrayList<>();
		for (int j = 0; j < input.length; j++) {
			values.add(new NumericAttributeValue(new NumericAttributeType(), input[i]));
		}
		return new SimpleInstance(values, target);
	}
	
	
	public static void main(String[] args) throws Exception {
		Random rand = new Random(42);
		
		ArrayList<IAttributeType<?>> types = new ArrayList<>();
		types.add(new NumericAttributeType());
		types.add(new NumericAttributeType());
		types.add(new NumericAttributeType());
		
		SimpleDataset ds = new SimpleDataset(new InstanceSchema(types, new NumericAttributeType()));
		
		// fill instances
		for (int i = 0; i < 10000; i++) {
			ArrayList<IAttributeValue<?>> values = new ArrayList<>();
			values.add(new NumericAttributeValue(new NumericAttributeType(), rand.nextDouble()));
			values.add(new NumericAttributeValue(new NumericAttributeType(), rand.nextDouble()));
			values.add(new NumericAttributeValue(new NumericAttributeType(), rand.nextDouble()));
			ds.add(new SimpleInstance(values, new NumericAttributeValue(new NumericAttributeType(), 12.0)));
		}
		
		ASamplingAlgorithm sampling = new GmeansSampling(42);
		sampling.setInput(ds);
		sampling.setSampleSize(100);
		
		IDataset<SimpleInstance> dsOut = sampling.call();
		
		System.out.println("Size: " + dsOut.size());
		for (SimpleInstance sam : dsOut) {
			System.out.println(sam);
		}
		
		
		
		
		
	}
	
	
	
	
}
