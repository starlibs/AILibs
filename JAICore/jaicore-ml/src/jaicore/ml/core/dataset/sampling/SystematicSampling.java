package jaicore.ml.core.dataset.sampling;

import java.util.Comparator;
import java.util.Random;

import jaicore.basic.algorithm.AlgorithmEvent;
import jaicore.ml.core.dataset.ContainsNonNumericAttributesException;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Implementation of Systematic Sampling: Sort datapoints and pick every k-th datapoint for the sample.
 * 
 * @author Lukas Brandt
 */
public class SystematicSampling extends ASamplingAlgorithm {

	private Random random;
	
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
		return null;
	}

	@Override
	public IDataset createSampleFromDataset(IDataset dataset) throws Exception {
		dataset.sort(this.datapointComparator);
		
		int startIndex = this.random.nextInt(dataset.size());
		int k = (int)Math.floor(dataset.size() / this.sampleSize);
		int i = 1;
		
		// TODO: Create real dataset.
		IDataset sample = null;
		
		while(sample.size() < this.sampleSize) {
			int e = (startIndex + (i++) * k) % dataset.size();
			sample.add(dataset.get(e));
		}
		
				
		return sample;
	}

}
