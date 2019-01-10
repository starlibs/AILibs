package jaicore.ml.core.dataset.sampling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.apache.commons.math3.ml.distance.ManhattanDistance;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.ml.clustering.GMeans;
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
public class GmeansSampling<I extends IInstance> extends ASamplingAlgorithm<I> {

	private GMeans<I> gMeansCluster;
	private List<CentroidCluster<I>> clusterResults;
	private int currentCluster = 0;

	private DistanceMeasure distanceMeassure = new ManhattanDistance();

	private long seed;

	/**
	 * Implementation of a sampling method using gmeans-clustering.
	 */
	public GmeansSampling(long seed) {
		this.seed = seed;
	}

	@Override
	public AlgorithmEvent nextWithException() throws AlgorithmException {
		switch (this.getState()) {
		case created:
			// Initialize variables
			this.sample = getInput().createEmpty();

			// create cluster
			gMeansCluster = new GMeans<I>(getInput(), distanceMeassure, seed);
			clusterResults = gMeansCluster.cluster();

			return this.activate();
		case active:
			if (currentCluster < clusterResults.size()) {
				CentroidCluster<I> cluster = clusterResults.get(currentCluster++);
				boolean same = true;
				for (int i = 1; i < cluster.getPoints().size(); i++) {
					if (!cluster.getPoints().get(i - 1).getTargetValue(Double.class)
							.equals(cluster.getPoints().get(i).getTargetValue(Double.class))) {
						same = false;
						break;
					}
				}
				if (same) {
					I near = cluster.getPoints().get(0);
					double dist = Double.MAX_VALUE;
					for (I p : cluster.getPoints()) {
						double newDist = distanceMeassure.compute(p.getPoint(), cluster.getCenter().getPoint());
						if (newDist < dist) {
							near = p;
							dist = newDist;
						}
					}
					sample.add(near);
				} else {
					// find a solution to not sample all points here
					for (int i = 0; i < cluster.getPoints().size(); i++) {
						sample.add(cluster.getPoints().get(i));
					}
				}
				return new SampleElementAddedEvent();
			} else {
				return this.terminate();
			}
		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new AlgorithmException("Expected sample size was not reached before termination");
			} else {
				return this.terminate();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state " + this.getState());
		}
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

		// GMeansStratiAmountSelectorAndAssigner<SimpleInstance> gm = new
		// GMeansStratiAmountSelectorAndAssigner<>(45);

		ASamplingAlgorithm<SimpleInstance> sampling = new GmeansSampling<SimpleInstance>(45);
		sampling.setInput(ds);
		sampling.setSampleSize(1000);

		IDataset<SimpleInstance> dsOut = sampling.call();

		System.out.println("Size: " + dsOut.size());
		for (SimpleInstance sam : dsOut) {
			System.out.println(sam);
		}

	}

}
