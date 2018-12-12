/**
 * 
 */
package jaicore.ml.tsc.filter;

import java.util.ArrayList;
import java.util.List;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import jaicore.ml.core.dataset.IDataset;
import jaicore.ml.core.dataset.attribute.IAttributeType;
import jaicore.ml.core.dataset.attribute.IAttributeValue;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeType;
import jaicore.ml.core.dataset.attribute.timeseries.TimeSeriesAttributeValue;


/**
 * @author Helen
 *
 */
public class zTransfrom implements IFilter {
	
	private INDArray means;
	private INDArray deviation;
	private ArrayList<Integer> index = new ArrayList<Integer>();

	
	//TODO alle methoden static ? 
	
	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#transform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset transform(IDataset input) throws IllegalArgumentException{
		
		if(input.isEmpty()) {
			throw new IllegalArgumentException("The input dataset was empty");
		}
		
//		if(!(input instanceof TimeSeriesDataset)) {
//			throw new IllegalArgumentException("This method only works with timeseries datasets");
//		}
//		
		fit(input);
		
		for(int i = 0; i < input.size(); i++) {
			for(int j = 0 ; j< index.size();j++) {
				IAttributeValue<TimeSeriesAttributeValue> val = input.get(i).getAttributeValue(index.get(j),TimeSeriesAttributeValue.class);
				INDArray timeseries = val.getValue().getValue();
				for(int k = 0; k < timeseries.length(); k++) {
					double normalizedValue = (timeseries.getDouble(k)-means.getDouble(i, j))/deviation.getDouble(i,j);
					timeseries.putScalar(k, normalizedValue);
				}
				//TODO new dataset or old one
				val.getValue().setValue(timeseries);
			}
		}
		
		return input;
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fit(jaicore.ml.core.dataset.IDataset)
	 */
	
	@Override
	public void fit(IDataset input) {
		
		
		List<IAttributeType<?>> attributes = input.getAttributeTypes();
		
		for(int i = 0; i< attributes.size() ; i++) {
			if( attributes.get(i) instanceof TimeSeriesAttributeType) {
				index.add(i);
			}
		}
		
		means = Nd4j.valueArrayOf(new int[] {index.size(),input.size()}, 0);
		deviation = Nd4j.valueArrayOf(new int[] {index.size(),input.size()}, 0);
		
		// get all timeseries and calculate mean and deviation 
		for(int i = 0; i < input.size(); i++) {
			for(int j = 0 ; j< index.size();j++) {
				IAttributeValue<TimeSeriesAttributeValue> val = input.get(i).getAttributeValue(index.get(j),TimeSeriesAttributeValue.class);
				INDArray timeseries = val.getValue().getValue();
				
				double meanval = mean(timeseries); 
				double deviationval = deviation(timeseries, meanval);
				
				means.putScalar(new int[] {i,j}, meanval);
				deviation.putScalar(new int[] {i,j}, deviationval);
			}
		}

	}
	//TODO in absolute value? 
	private double mean(INDArray input) {
		double mean = 0;
		for(int i = 0; i<input.length(); i++) {
			mean += input.getDouble(i);
		}
		
		return mean/input.length();
	}
	
	private double deviation(INDArray input, double mean) {
		double deviation = 0;
		//sum of squared difference to mean
		for(int i = 0; i<input.length(); i++) {
			deviation += Math.pow(input.getDouble(i)-mean, 2);
		}
		//TODO what if the timeseries is only one entry long it would divide by zero
		deviation = deviation/(input.length()-1);
		//square route to get deviation 
		return Math.sqrt(deviation);
	}

	/* (non-Javadoc)
	 * @see jaicore.ml.tsc.filter.IFilter#fitTransform(jaicore.ml.core.dataset.IDataset)
	 */
	@Override
	public IDataset fitTransform(IDataset input) {
		//TODO call fit in transform or not ?
		return null;
	}

}
