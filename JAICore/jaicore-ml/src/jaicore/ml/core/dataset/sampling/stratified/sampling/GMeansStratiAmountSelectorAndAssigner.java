package jaicore.ml.core.dataset.sampling.stratified.sampling;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.IInstance;

/**
 * Combined strati amount selector and strati assigner vie g-means.
 * 
 * @author Lukas Brandt
 */
public class GMeansStratiAmountSelectorAndAssigner implements IStratiAssigner, IStratiAmountSelector {

	private int numCPUs = 1;
	
	@Override
	public int selectStratiAmount(IDataset dataset) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNumCPUs(int numberOfCPUs) {
		this.numCPUs = numberOfCPUs;
	}

	@Override
	public int getNumCPUs() {
		return this.numCPUs;
	}

	@Override
	public void init(IDataset dataset, int stratiAmount) {
		// TODO Auto-generated method stub

	}

	@Override
	public int assignToStrati(IInstance datapoint) {
		// TODO Auto-generated method stub
		return 0;
	}

}
