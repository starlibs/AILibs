package jaicore.ml.tsc.filter;

import org.nd4j.linalg.api.ndarray.INDArray;
import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SAX implements IFilter {
	
	private INDArray alphabet;
	private boolean fitted;
	int wordLength;
	
	public SAX(INDArray alphabet, int wordLength) {
		this.alphabet  = alphabet;
		this.wordLength = wordLength;
	}

	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset can not be empty");
		}
		if(!(input instanceof TimeSeriesDataset)){
			throw new IllegalArgumentException("This method only works for TimeSeriesDatasets");
		}
		
		ZTransformer ztransform = new ZTransformer();
		TimeSeriesDataset zTransformedDataset = (TimeSeriesDataset)ztransform.fitTransform(input);
		return null;
	}

	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub
		fitted = true;
	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		fit(input);
		fitted = true;
		return transform(input);
	}

}
