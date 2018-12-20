package jaicore.ml.core.dataset.sampling;

import java.util.Comparator;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.basic.algorithm.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.AlgorithmState;
import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.IInstance;

/**
 * Implementation of Systematic Sampling: Sort datapoints and pick every k-th datapoint for the sample.
 * 
 * @author Lukas Brandt
 */
public class SystematicSampling<I extends IInstance> extends ASamplingAlgorithm<I> {

	private Random random;
	private int k;
	private int startIndex;
	private int index;
	
	// Default Comparator to sort datapoints by their vector representation.
	private Comparator<IInstance> datapointComparator = new Comparator<IInstance>() {
		@Override
		public int compare(IInstance o1, IInstance o2) {
			double[] v1, v2;
			try {
				v1 = o1.getAsDoubleVector();
				v2 = o2.getAsDoubleVector();
			} catch (ContainsNonNumericAttributesException e) {
				e.printStackTrace();
				return 0;
			}
			for (int i = 0; i < Math.min(v1.length, v2.length); i++) {
				int c = Double.compare(v1[i], v2[i]);
				if (c != 0) {
					return c;
				}
			}
			return 0;
		}
		
	};
	
	/** Simple constructor that uses the default datapoint comparator. 
	 * @param random Random Object for determining the sampling start point.
	 */
	public SystematicSampling(Random random) {
		this.random = random;
	}
	
	/**
	 * Constructor for a custom datapoint comparator.
	 * @param random Random Object for determining the sampling start point.
	 * @param datapointComparator Comparator to sort the dataset.
	 */
	public SystematicSampling(Random random, Comparator<IInstance> datapointComparator) {
		this.random = random;
		this.datapointComparator = datapointComparator;
	}
	
	@Override
	public AlgorithmEvent nextWithException() throws Exception {
		switch (this.getState()) {
		case created:
			// Initialize variables and sort dataset.
			this.sample = getInput().createEmpty();
			this.getInput().sort(this.datapointComparator);
			this.startIndex = this.random.nextInt(this.getInput().size());
			this.k = (int)Math.floor(this.getInput().size() / this.sampleSize);
			this.index = 0;
			this.setState(AlgorithmState.active);
			return new AlgorithmInitializedEvent();			
		case active:
			// If the sample size is not reached yet, add the next datapoint from the systematic sampling method.
			if (this.sample.size() < this.sampleSize) {
				int e = (startIndex + (this.index++) * k) % this.getInput().size();
				this.sample.add(this.getInput().get(e));
				return new SampleElementAddedEvent();
			} else {
				this.setState(AlgorithmState.inactive);
				return new AlgorithmFinishedEvent();
			}
		case inactive: {
			if (this.sample.size() < this.sampleSize) {
				throw new Exception("Expected sample size was not reached before termination");
			} else {
				return new AlgorithmFinishedEvent();
			}
		}
		default:
			throw new IllegalStateException("Unknown algorithm state "+ this.getState());
		}
	}

}
