package jaicore.ml.tsc.filter;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class DFA implements IFilter {

	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		
		
		return null;
	}

	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub

	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

}
