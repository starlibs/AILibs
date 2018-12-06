/**
 * 
 */
package jaicore.ml.tsc.filter;

import java.util.ArrayList;

import jaicore.ml.core.dataset.IDataset;

/**
 * @author Helen
 *
 */
public class zTransfrom implements IFilter {
	
	private ArrayList<Double> mean = new ArrayList<Double>();
	private ArrayList<Double> derivation = new ArrayList<Double>();
	
	public ArrayList<Double> getMean() {
		return mean;
	}

	public ArrayList<Double> getDerivation() {
		return derivation;
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException{
		
		ArrayList<double[]> tsToBeNormalized = new ArrayList<double[]>();
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset was empty");
		}
		
		for(int i = 0; i < input.size(); i++) {
			tsToBeNormalized.add(input.get(i).getAsDoubleVector());			
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fit(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fitTransform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset fitTransform(IDataset input) {
		// TODO Auto-generated method stub
		return null;
	}

}
