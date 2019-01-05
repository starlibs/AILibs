package jaicore.ml.tsc.filter;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.TimeSeriesDataset;
import jaicore.ml.tsc.exceptions.NoneFittedFilterExeception;

public class SFA implements IFilter {

	private INDArray alphabet;
	private int wordlength; 
	private TimeSeriesDataset DFTDataset;
	boolean fitted = false;
	
	
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		
		
		return null;
	}

	@Override
	public void fit(IDataset input) {
		// TODO Auto-generated method stub
	
		if(!(input instanceof TimeSeriesDataset)) {
			throw new IllegalArgumentException("This method only supports Timesereies datasets");
		}
		
		DFT dftFilter = new DFT();
		try {
			DFTDataset = (TimeSeriesDataset) dftFilter.fitTransform(input);
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoneFittedFilterExeception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		fitted = true;
	}

	@Override
	public IDataset fitTransform(IDataset input) throws IllegalArgumentException, NoneFittedFilterExeception {
		// TODO Auto-generated method stub
		return null;
	}

}
